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
import monitor.queryobject.Criteria;
import monitor.queryobject.QueryObject;

/**
 * The application's main frame.
 */
public class TransactionMonitorView extends FrameView {
    private List dbList = new ArrayList();
    private final String[] paramsTableHeader = {"Key","Value"};
    private final String[] criteriaTableHeader = {"Key","Operator","Value"};
    private final String[] operators = {Criteria.EQ, Criteria.GT, Criteria.LT, Criteria.LIKE};
    private final String[] operations = {QueryObject.INSERT, QueryObject.DELETE, QueryObject.UPDATE};
    public TransactionMonitorView(SingleFrameApplication app) {
        super(app);
        //bazy danych
        Vector db1 = new Vector(4);
        db1.add("MYSQL");
        db1.add("jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_6");
        db1.add("ares-system_6");
        db1.add("TEsttest1");
        dbList.add(db1);
        Vector db2 = new Vector(4);
        db2.add("MYSQL-XA");
        db2.add("jdbc:mysql://sql.ares-system.nazwa.pl:3305/ares-system_7");
        db2.add("ares-system_7");
        db2.add("TEsttest1");
        dbList.add(db2);
//        Vector db1 = new Vector(4);
//        db3.add("MYSQL");
//        db3.add(db1);
//        db3.add(db1);
//        db3.add(db1);
        //bazy danych - koniec


        operationParametersModel = new DefaultTableModel();

        for (int i=0; i<paramsTableHeader.length;i++){
            operationParametersModel.addColumn(paramsTableHeader[i]);
        }

        criteriaTableModel = new DefaultTableModel();
        for (int i=0; i<criteriaTableHeader.length; i++){
            criteriaTableModel.addColumn(criteriaTableHeader[i]);
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
    private ComboBoxModel operationDbComboMOdel(){
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (int i=0; i<dbList.size(); i++){
                Vector db = (Vector) dbList.get(i);
                model.addElement(db.get(1));
            }
            return model;
    }
    private ComboBoxModel operatorsComboModel(){
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (int i=0; i<operators.length; i++){
                model.addElement(operators[i]);
            }
            return model;
    }
    private ComboBoxModel operationsComboModel(){
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (int i=0; i<operations.length; i++){
                model.addElement(operations[i]);
            }
        return model;
    }
    /**
     * Funkcja zapisuje dane połączenia do nowej bazy w pliku konfiguracyjnym
     */
    public void addDb() {
        Vector db = new Vector(4);
        db.add(this.dbTypeComboBox.getSelectedItem());
        db.add(this.dbConnectionUrlTextField.getText());
        db.add(this.dbUserTextField.getText());
        db.add(this.dbPasswordField.getPassword());
        dbList.add(db);
        this.operationDbComboBox.setModel(operationDbComboMOdel());
        this.operationDbComboBox.setSelectedIndex(dbList.size()-1);
        dbConnectionUrlTextField.setText("");
        dbUserTextField.setText("");
        dbPasswordField.setText("");
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
    /**
     * 
     */
    public void resetOperations() {
    }

    /**
     * Funkcja odpalana z poziomu GUI. Uruchamia procedurę z klasy TransactionLogic
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
        jSeparator1 = new javax.swing.JSeparator();
        dbTypeComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        dbConnectionUrlTextField = new javax.swing.JTextField();
        dbUserTextField = new javax.swing.JTextField();
        dbPasswordField = new javax.swing.JPasswordField();
        addDbButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
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
        jButton2 = new javax.swing.JButton();
        addParemeterButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
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
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        jSeparator1.setName("jSeparator1"); // NOI18N

        dbTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "MYSQL", "MYSQL XA" }));
        dbTypeComboBox.setName("dbTypeComboBox"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(monitor.TransactionMonitorApp.class).getContext().getResourceMap(TransactionMonitorView.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        dbConnectionUrlTextField.setText(resourceMap.getString("dbConnectionUrlTextField.text")); // NOI18N
        dbConnectionUrlTextField.setName("dbConnectionUrlTextField"); // NOI18N

        dbUserTextField.setText(resourceMap.getString("dbUserTextField.text")); // NOI18N
        dbUserTextField.setName("dbUserTextField"); // NOI18N

        dbPasswordField.setText(resourceMap.getString("dbPasswordField.text")); // NOI18N
        dbPasswordField.setName("dbPasswordField"); // NOI18N

        addDbButton.setText(resourceMap.getString("dbAddButton.text")); // NOI18N
        addDbButton.setName("dbAddButton"); // NOI18N
        addDbButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDbButtonActionPerformed(evt);
            }
        });

        jLabel5.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

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

        jButton2.setText(resourceMap.getString("startCompositeTransactionButton.text")); // NOI18N
        jButton2.setName("startCompositeTransactionButton"); // NOI18N

        addParemeterButton.setText(resourceMap.getString("operationAddPrameterButton.text")); // NOI18N
        addParemeterButton.setName("operationAddPrameterButton"); // NOI18N
        addParemeterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addParemeterButtonActionPerformed(evt);
            }
        });

        jSeparator2.setName("jSeparator2"); // NOI18N

        jButton4.setText(resourceMap.getString("opearationAddButton.text")); // NOI18N
        jButton4.setName("opearationAddButton"); // NOI18N

        jButton5.setText(resourceMap.getString("operationResetButton.text")); // NOI18N
        jButton5.setName("operationResetButton"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        operationParametersTable.setModel(operationParametersModel);
        operationParametersTable.setName("operationParametersTable"); // NOI18N
        jScrollPane1.setViewportView(operationParametersTable);

        operationTableTextField.setText(resourceMap.getString("operationTableTextField.text")); // NOI18N
        operationTableTextField.setName("operationTableTextField"); // NOI18N

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        criterionsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        criterionsTable.setName("criterionsTable"); // NOI18N
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

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(104, 104, 104)
                                .addComponent(jLabel1))
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(dbPasswordField)
                            .addComponent(dbUserTextField)
                            .addComponent(dbTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(dbConnectionUrlTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addDbButton))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jLabel6))
                            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 635, Short.MAX_VALUE))))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 447, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 616, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
                        .addGap(503, 503, 503)))
                .addContainerGap())
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(84, 84, 84)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel13)
                        .addComponent(jLabel9)))
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
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
                        .addComponent(criteriaValueTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(addCriteriaButton, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(70, 70, 70))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addComponent(operationKeyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(operationValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(addParemeterButton)
                        .addContainerGap())
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
                            .addComponent(operationTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton5))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                                .addComponent(operationDbComboBox, 0, 280, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel12)
                                .addGap(18, 18, 18)
                                .addComponent(operationTableTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(45, 45, 45))))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dbTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(dbConnectionUrlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(dbUserTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(dbPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addDbButton))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(operationDbComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(operationTableTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(operationTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)))
                    .addComponent(jButton5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(operationKeyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(operationValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addParemeterButton)
                    .addComponent(jLabel10))
                .addGap(13, 13, 13)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addGap(143, 143, 143)
                        .addComponent(jButton4))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(criteriaKeyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addCriteriaButton)
                            .addComponent(jLabel15)
                            .addComponent(criteriaOperatorCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16)
                            .addComponent(criteriaValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(jButton2)
                .addGap(106, 106, 106))
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

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 655, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 485, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

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

    private void setCriterionsEnabled(boolean enabled){
        criterionsTable.setEnabled(enabled);
        criteriaKeyTextField.setEnabled(enabled);
        criteriaOperatorCombo.setEnabled(enabled);
        criteriaValueTextField.setEnabled(enabled);
        addCriteriaButton.setEnabled(enabled);
    }
    private void setParametersEnabled(boolean enabled){
        operationParametersTable.setEnabled(enabled);
        operationKeyTextField.setEnabled(enabled);
        operationValueTextField.setEnabled(enabled);
        addParemeterButton.setEnabled(enabled);
    }
    private void operationTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_operationTypeComboBoxActionPerformed
      String operationType = (String)this.operationTypeComboBox.getSelectedItem();
      if (operationType.equals(QueryObject.INSERT)){
            setParametersEnabled(true);
            setCriterionsEnabled(false);
      } else if (operationType.equals(QueryObject.DELETE)){
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addCriteriaButton;
    private javax.swing.JButton addDbButton;
    private javax.swing.JButton addParemeterButton;
    private javax.swing.JTextField criteriaKeyTextField;
    private javax.swing.JComboBox criteriaOperatorCombo;
    private javax.swing.JTextField criteriaValueTextField;
    private javax.swing.JTable criterionsTable;
    private javax.swing.JTextField dbConnectionUrlTextField;
    private javax.swing.JPasswordField dbPasswordField;
    private javax.swing.JComboBox dbTypeComboBox;
    private javax.swing.JTextField dbUserTextField;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JComboBox operationDbComboBox;
    private javax.swing.JTextField operationKeyTextField;
    private javax.swing.JTable operationParametersTable;
    private javax.swing.JTextField operationTableTextField;
    private javax.swing.JComboBox operationTypeComboBox;
    private javax.swing.JTextField operationValueTextField;
    private javax.swing.JProgressBar progressBar;
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
    private JDialog aboutBox;


}
