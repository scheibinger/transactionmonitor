/*
 * TransactionMonitorView.java
 */
package monitor;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import monitor.MyLogger.MyLogger;
import monitor.queryobject.Criteria;
import monitor.queryobject.QueryObject;
import monitor.queryobject.QueryParameter;

/**
 * The application's main frame.
 */
public class TransactionMonitorView extends FrameView {

    private Vector dbList = new Vector();
    private final String[] paramsTableHeader = {"Key", "Value"};
    private final String[] criteriaTableHeader = {"Key", "Operator", "Value"};
    private final String[] operationsTableHeader = {"Database", "Query"};
    private final String[] operators = {Criteria.EQ, Criteria.GT, Criteria.LT, Criteria.LIKE};
    private final String[] operations = {QueryObject.INSERT, QueryObject.DELETE, QueryObject.UPDATE};

    public TransactionMonitorView(SingleFrameApplication app) {
        super(app);
        //bazy danych

//        Vector db1 = new Vector(4);
//        db3.add("MYSQL");
//        db3.add(db1);
//        db3.add(db1);
//        db3.add(db1);
        //bazy danych - koniec

        TransactionLogic.getInstance().openTransaction();
        operationParametersModel = new DefaultTableModel();

        for (int i = 0; i < paramsTableHeader.length; i++) {
            operationParametersModel.addColumn(paramsTableHeader[i]);
        }

        criteriaTableModel = new DefaultTableModel();
        for (int i = 0; i < criteriaTableHeader.length; i++) {
            criteriaTableModel.addColumn(criteriaTableHeader[i]);
        }
        operationsTableModel = new DefaultTableModel();
        for (int i = 0; i < operationsTableHeader.length; i++) {
            operationsTableModel.addColumn(operationsTableHeader[i]);
        }
        initComponents();


        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        setCriterionsEnabled(false);
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = TransactionMonitorApp.getApplication().getMainFrame();
            aboutBox = new TransactionMonitorAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        TransactionMonitorApp.getApplication().show(aboutBox);
    }

    private ComboBoxModel operationDbComboMOdel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        dbList = TransactionLogic.getInstance().getDbConnectionList();
        for (int i = 0; i < dbList.size(); i++) {
            DBConnectionData db = (DBConnectionData) dbList.get(i);
            model.addElement(db.getDesc());

        }
        return model;
    }


    private ComboBoxModel operatorsComboModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (int i = 0; i < operators.length; i++) {
            model.addElement(operators[i]);
        }
        return model;
    }

    private ComboBoxModel operationsComboModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (int i = 0; i < operations.length; i++) {
            model.addElement(operations[i]);
        }
        return model;
    }

    /**
     * Funkcja zapisuje dane poĹ‚Ä…czenia do nowej bazy w pliku konfiguracyjnym
     */
    public void addDb() {
        String driver = "";
        String url="";
        String dbType = this.dbTypeComboBox.getSelectedItem().toString();

        if (dbType.equals("mysql")) {
            driver = "com.mysql.jdbc.Driver";
            url = "jdbc:mysql://"+this.dbHostTextField.getText()+":"+this.dbPortTextField.getText()+"/"+this.dbNameTextField.getText();
        } else if (dbType.equals("postgresql")) {
            driver = "org.postgresql.Driver";
            url = "jdbc:postgresql://"+this.dbHostTextField.getText()+":"+this.dbPortTextField.getText()+"/"+this.dbNameTextField.getText();
        }

        String user = this.dbUserTextField.getText();
        String password = this.dbPasswordField.getPassword().toString();
        String desc = this.dbDescriptionTextField.getText()+" "+this.dbProtocolComboBox.getSelectedItem().toString();


        String type = this.dbProtocolComboBox.getSelectedItem().toString();
        DBConnectionData db = new DBConnectionData(driver, url, user, password, desc, type, dbType);

        dbList.add(db);
        this.operationDbComboBox.setModel(operationDbComboMOdel());
        this.operationDbComboBox.setSelectedIndex(dbList.size() - 1);
        dbHostTextField.setText("");
        dbUserTextField.setText("");
        dbPasswordField.setText("");
    }

    /**
     * Funkcja tworzy nowÄ… transakcje globalna
     */
    public void resetGlobalTransaction() {
        TransactionLogic.getInstance().openTransaction();

    }

    /**
     * 
     */
    public void addParam() {
        //dodajemy wiersz do tabeli
        Vector tmpRow = new Vector(2);
        tmpRow.add(this.operationKeyTextField.getText());
        tmpRow.add(this.operationValueTextField.getText());
        this.operationParametersModel.addRow(tmpRow);
        operationKeyTextField.setText("");
        operationValueTextField.setText("");
    }

    public void addCriteira() {
        Vector tmpRow = new Vector(3);
        tmpRow.add(this.criteriaKeyTextField.getText());
        tmpRow.add(this.criteriaOperatorCombo.getSelectedItem());
        tmpRow.add(this.criteriaValueTextField.getText());
        this.criteriaTableModel.addRow(tmpRow);
        criteriaKeyTextField.setText("");
        criteriaValueTextField.setText("");
    }

    private void addOperation() {
        String queryType = (String) this.operationTypeComboBox.getSelectedItem();
        String tableName = this.operationTableTextField.getText();
        List criterions = new ArrayList();
        for (int i=0; i<criteriaTableModel.getRowCount();i++){
         Criteria cr = new Criteria((String)criteriaTableModel.getValueAt(i, 0),
                                  (String)criteriaTableModel.getValueAt(i, 1),
                                  (String)criteriaTableModel.getValueAt(i, 2));
         criterions.add(cr);
        }
        List params = new ArrayList();
        for (int i=0; i<operationParametersModel.getRowCount(); i++){
            QueryParameter qp = new QueryParameter(
                                  (String)operationParametersModel.getValueAt(i, 0),
                                  (String)operationParametersModel.getValueAt(i, 1));
            params.add(qp);
        }
        QueryObject qo = new QueryObject(queryType, tableName, criterions,params);
        String query = qo.getQuery();
        Vector v = new Vector(2);
        v.add(this.operationDbComboBox.getSelectedItem());
        v.add(query);
        this.operationsTableModel.addRow(v);
            
            int sindex = operationDbComboBox.getSelectedIndex();
            DBConnectionData dbcd = (DBConnectionData) TransactionLogic.getInstance().getDbConnectionList().elementAt(sindex);
            TransactionLogic.getInstance().addAtomicTransaction(qo,dbcd);
        try {
        System.out.println(qo.generateQuery());
        } catch (Exception e) {}


    }

    /**
     * 
     */
    public void resetOperations() {
       this.clearTable(criteriaTableModel);
       this.clearTable(operationParametersModel);

    }
    public void resetAddedOperations(){
        this.clearTable(operationsTableModel);
    }
    private void clearTable(DefaultTableModel model){
        while (model.getRowCount() > 0) {
            model.removeRow(model.getRowCount()-1);
        }
    }
    /**
     * Funkcja odpalana z poziomu GUI. Uruchamia procedurÄ™ z klasy TransactionLogic
     */
    public void startCompositeTransaction() {
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        operationDbComboBox = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        operationTypeComboBox = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        operationKeyTextField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        operationValueTextField = new javax.swing.JTextField();
        startCompositeTransactionButton = new javax.swing.JButton();
        addParemeterButton = new javax.swing.JButton();
        addOperationButton = new javax.swing.JButton();
        resetOperationButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        operationParametersTable = new javax.swing.JTable();
        operationTableTextField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        criterionsTable = new javax.swing.JTable();
        jLabel14 = new javax.swing.JLabel();
        criteriaKeyTextField = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        criteriaOperatorCombo = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        criteriaValueTextField = new javax.swing.JTextField();
        addCriteriaButton = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        operationsTable = new javax.swing.JTable();
        newCompositeTransaction = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        dbUserTextField = new javax.swing.JTextField();
        dbHostTextField = new javax.swing.JTextField();
        dbTypeComboBox = new javax.swing.JComboBox();
        dbNameTextField = new javax.swing.JTextField();
        addDbButton = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        dbPortTextField = new javax.swing.JTextField();
        dbPasswordField = new javax.swing.JPasswordField();
        dbProtocolComboBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        dbDescriptionTextField = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        logTextArea = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        statusPanel = new javax.swing.JPanel();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();

        mainPanel.setName("mainPanel"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(monitor.TransactionMonitorApp.class).getContext().getResourceMap(TransactionMonitorView.class);
        jLabel6.setFont(resourceMap.getFont("jLabel6.font")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        operationDbComboBox.setModel(this.operationDbComboMOdel());
        operationDbComboBox.setName("operationDbComboBox"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        operationTypeComboBox.setModel(operationsComboModel());
        operationTypeComboBox.setName("operationTypeComboBox"); // NOI18N
        operationTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                operationTypeComboBoxActionPerformed(evt);
            }
        });

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        operationKeyTextField.setText(resourceMap.getString("operationKeyTextField.text")); // NOI18N
        operationKeyTextField.setName("operationKeyTextField"); // NOI18N

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        operationValueTextField.setText(resourceMap.getString("operationValueTextField.text")); // NOI18N
        operationValueTextField.setName("operationValueTextField"); // NOI18N

        startCompositeTransactionButton.setText(resourceMap.getString("startCompositeTransactionButton.text")); // NOI18N
        startCompositeTransactionButton.setName("startCompositeTransactionButton"); // NOI18N
        startCompositeTransactionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startCompositeTransactionButtonActionPerformed(evt);
            }
        });

        addParemeterButton.setText(resourceMap.getString("operationAddPrameterButton.text")); // NOI18N
        addParemeterButton.setName("operationAddPrameterButton"); // NOI18N
        addParemeterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addParemeterButtonActionPerformed(evt);
            }
        });

        addOperationButton.setText(resourceMap.getString("opearationAddButton.text")); // NOI18N
        addOperationButton.setName("opearationAddButton"); // NOI18N
        addOperationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addOperationButtonActionPerformed(evt);
            }
        });

        resetOperationButton.setText(resourceMap.getString("operationResetButton.text")); // NOI18N
        resetOperationButton.setName("operationResetButton"); // NOI18N
        resetOperationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetOperationButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        operationParametersTable.setModel(operationParametersModel);
        operationParametersTable.setName("operationParametersTable"); // NOI18N
        operationParametersTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                operationParametersTableKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(operationParametersTable);

        operationTableTextField.setText(resourceMap.getString("operationTableTextField.text")); // NOI18N
        operationTableTextField.setName("operationTableTextField"); // NOI18N

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        criterionsTable.setModel(criteriaTableModel);
        criterionsTable.setName("criterionsTable"); // NOI18N
        criterionsTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                criterionsTableKeyPressed(evt);
            }
        });
        jScrollPane2.setViewportView(criterionsTable);

        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        criteriaKeyTextField.setText(resourceMap.getString("criteriaKeyTextField.text")); // NOI18N
        criteriaKeyTextField.setName("criteriaKeyTextField"); // NOI18N

        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        criteriaOperatorCombo.setModel(operatorsComboModel());
        criteriaOperatorCombo.setName("criteriaOperatorCombo"); // NOI18N

        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        criteriaValueTextField.setText(resourceMap.getString("criteriaValueTextField.text")); // NOI18N
        criteriaValueTextField.setName("criteriaValueTextField"); // NOI18N

        addCriteriaButton.setText(resourceMap.getString("addCriteriaButton.text")); // NOI18N
        addCriteriaButton.setName("addCriteriaButton"); // NOI18N
        addCriteriaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCriteriaButtonActionPerformed(evt);
            }
        });

        jLabel21.setText(resourceMap.getString("jLabel21.text")); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        operationsTable.setModel(operationsTableModel);
        operationsTable.setName("operationsTable"); // NOI18N
        operationsTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                operationsTableKeyPressed(evt);
            }
        });
        jScrollPane3.setViewportView(operationsTable);

        newCompositeTransaction.setActionCommand(resourceMap.getString("newCompositeTransaction.actionCommand")); // NOI18N
        newCompositeTransaction.setLabel(resourceMap.getString("newCompositeTransaction.label")); // NOI18N
        newCompositeTransaction.setName("newCompositeTransaction"); // NOI18N
        newCompositeTransaction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newCompositeTransactionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel7)
                            .addComponent(jLabel9)
                            .addComponent(jLabel13)
                            .addComponent(jLabel21))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(addOperationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 403, Short.MAX_VALUE)
                                .addComponent(resetOperationButton))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(criteriaKeyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(criteriaOperatorCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(criteriaValueTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(addCriteriaButton, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addComponent(operationKeyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(operationValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 260, Short.MAX_VALUE)
                                .addComponent(addParemeterButton))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
                            .addComponent(operationTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(operationDbComboBox, 0, 500, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel12)
                                .addGap(10, 10, 10)
                                .addComponent(operationTableTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 671, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(startCompositeTransactionButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(newCompositeTransaction))
                    .addComponent(jLabel6))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addGap(31, 31, 31)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(operationDbComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(operationTableTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(operationTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(operationKeyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11)
                            .addComponent(operationValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10)
                            .addComponent(addParemeterButton)))
                    .addComponent(jLabel9))
                .addGap(13, 13, 13)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(criteriaKeyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15)
                            .addComponent(criteriaOperatorCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16)
                            .addComponent(criteriaValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addCriteriaButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addOperationButton)
                            .addComponent(resetOperationButton)))
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newCompositeTransaction)
                    .addComponent(startCompositeTransactionButton))
                .addGap(137, 137, 137))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        jLabel5.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        jLabel20.setText(resourceMap.getString("jLabel20.text")); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N

        dbUserTextField.setText(resourceMap.getString("dbUserTextField.text")); // NOI18N
        dbUserTextField.setName("dbUserTextField"); // NOI18N

        dbHostTextField.setText(resourceMap.getString("dbHostTextField.text")); // NOI18N
        dbHostTextField.setName("dbHostTextField"); // NOI18N

        dbTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "MySql", "PostgreSql" }));
        dbTypeComboBox.setName("dbTypeComboBox"); // NOI18N

        dbNameTextField.setName("dbNameTextField"); // NOI18N

        addDbButton.setText(resourceMap.getString("dbAddButton.text")); // NOI18N
        addDbButton.setName("dbAddButton"); // NOI18N
        addDbButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDbButtonActionPerformed(evt);
            }
        });

        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N

        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        dbPortTextField.setName("dbPortTextField"); // NOI18N

        dbPasswordField.setText(resourceMap.getString("dbPasswordField.text")); // NOI18N
        dbPasswordField.setName("dbPasswordField"); // NOI18N

        dbProtocolComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2PC", "XA" }));
        dbProtocolComboBox.setName("dbProtocolComboBox"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        dbDescriptionTextField.setName("dbDescriptionTextField"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(65, 65, 65)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel20))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(dbNameTextField, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dbUserTextField)
                            .addComponent(dbHostTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                            .addComponent(dbTypeComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(34, 34, 34)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel18)
                                    .addComponent(jLabel17)
                                    .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(dbProtocolComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(dbPortTextField)
                                    .addComponent(dbPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(dbDescriptionTextField))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(addDbButton)))))
                .addContainerGap(78, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dbProtocolComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dbPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dbPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addGap(8, 8, 8)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel19)
                            .addComponent(dbDescriptionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addDbButton))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(dbTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dbHostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dbUserTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(8, 8, 8)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dbNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel20)))
                    .addComponent(jLabel1))
                .addContainerGap(403, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        logTextArea.setColumns(20);
        logTextArea.setRows(5);
        logTextArea.setEditable(false);
        logTextArea.setName("logTextArea"); // NOI18N
        MyLogger.getLogger();
        MyLogger.setOutputTextArea(logTextArea);
        jScrollPane4.setViewportView(logTextArea);

        jButton1.setLabel(resourceMap.getString("clearLogButton.label")); // NOI18N
        jButton1.setName("clearLogButton"); // NOI18N
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 770, Short.MAX_VALUE)
                    .addComponent(jButton1))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        statusPanel.setName("statusPanel"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 625, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 795, Short.MAX_VALUE)
            .addComponent(statusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 591, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(statusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(monitor.TransactionMonitorApp.class).getContext().getActionMap(TransactionMonitorView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void addParemeterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addParemeterButtonActionPerformed

        // TODO add your handling code here:
        this.addParam();

}//GEN-LAST:event_addParemeterButtonActionPerformed

    private void addDbButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDbButtonActionPerformed
        this.addDb();
    }//GEN-LAST:event_addDbButtonActionPerformed

    private void setCriterionsEnabled(boolean enabled) {
        criterionsTable.setEnabled(enabled);
        criteriaKeyTextField.setEnabled(enabled);
        criteriaOperatorCombo.setEnabled(enabled);
        criteriaValueTextField.setEnabled(enabled);
        addCriteriaButton.setEnabled(enabled);
    }

    private void setParametersEnabled(boolean enabled) {
        operationParametersTable.setEnabled(enabled);
        operationKeyTextField.setEnabled(enabled);
        operationValueTextField.setEnabled(enabled);
        addParemeterButton.setEnabled(enabled);
    }
    private void operationTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_operationTypeComboBoxActionPerformed
        String operationType = (String) this.operationTypeComboBox.getSelectedItem();
        if (operationType.equals(QueryObject.INSERT)) {
            setParametersEnabled(true);
            setCriterionsEnabled(false);
        } else if (operationType.equals(QueryObject.DELETE)) {
            setParametersEnabled(false);
            setCriterionsEnabled(true);
        } else if (operationType.equals(QueryObject.UPDATE)) {
            setParametersEnabled(true);
            setCriterionsEnabled(true);
        }
    }//GEN-LAST:event_operationTypeComboBoxActionPerformed

    private void addCriteriaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCriteriaButtonActionPerformed
        this.addCriteira();
    }//GEN-LAST:event_addCriteriaButtonActionPerformed

    private void operationParametersTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_operationParametersTableKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            this.operationParametersModel.removeRow(this.operationParametersTable.getSelectedRow());
        }
    }//GEN-LAST:event_operationParametersTableKeyPressed

    private void criterionsTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_criterionsTableKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            this.criteriaTableModel.removeRow(this.criterionsTable.getSelectedRow());
        }
    }//GEN-LAST:event_criterionsTableKeyPressed

    private void addOperationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addOperationButtonActionPerformed
        this.addOperation();
    }//GEN-LAST:event_addOperationButtonActionPerformed

    private void resetOperationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetOperationButtonActionPerformed
      this.resetOperations();
    }//GEN-LAST:event_resetOperationButtonActionPerformed

    private void operationsTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_operationsTableKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            this.operationsTableModel.removeRow(this.operationsTable.getSelectedRow());
        }
}//GEN-LAST:event_operationsTableKeyPressed

    private void startCompositeTransactionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startCompositeTransactionButtonActionPerformed
        // TODO add your handling code here:
        if(TransactionLogic.getInstance().startTransaction()){
            System.out.print("jest ok!");
        }else
            System.out.print("gowno!");
}//GEN-LAST:event_startCompositeTransactionButtonActionPerformed

	private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
		// TODO add your handling code here:
	}//GEN-LAST:event_jButton1MouseClicked

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		logTextArea.setText("");
	}//GEN-LAST:event_jButton1ActionPerformed
    private void newCompositeTransactionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newCompositeTransactionActionPerformed
        // TODO add your handling code here:
        this.resetOperations();
        this.resetAddedOperations();
        TransactionLogic.getInstance().restartTransaction();
    }//GEN-LAST:event_newCompositeTransactionActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addCriteriaButton;
    private javax.swing.JButton addDbButton;
    private javax.swing.JButton addOperationButton;
    private javax.swing.JButton addParemeterButton;
    private javax.swing.JTextField criteriaKeyTextField;
    private javax.swing.JComboBox criteriaOperatorCombo;
    private javax.swing.JTextField criteriaValueTextField;
    private javax.swing.JTable criterionsTable;
    private javax.swing.JTextField dbDescriptionTextField;
    private javax.swing.JTextField dbHostTextField;
    private javax.swing.JTextField dbNameTextField;
    private javax.swing.JPasswordField dbPasswordField;
    private javax.swing.JTextField dbPortTextField;
    private javax.swing.JComboBox dbProtocolComboBox;
    private javax.swing.JComboBox dbTypeComboBox;
    private javax.swing.JTextField dbUserTextField;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea logTextArea;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton newCompositeTransaction;
    private javax.swing.JComboBox operationDbComboBox;
    private javax.swing.JTextField operationKeyTextField;
    private javax.swing.JTable operationParametersTable;
    private javax.swing.JTextField operationTableTextField;
    private javax.swing.JComboBox operationTypeComboBox;
    private javax.swing.JTextField operationValueTextField;
    private javax.swing.JTable operationsTable;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton resetOperationButton;
    private javax.swing.JButton startCompositeTransactionButton;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private DefaultTableModel criteriaTableModel;
    private DefaultTableModel operationParametersModel;
    private DefaultTableModel operationsTableModel;
    private JDialog aboutBox;

}
