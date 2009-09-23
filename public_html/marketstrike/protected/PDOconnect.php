<?php
/**
 * Klasa obsługująca połączenie do bazy gdy uzywamy procedure skladowych
 *
 */

class PDOconnect extends TModule{
    private static $_host = 'sql.gofferson.nazwa.pl';
    private static $_dbname = 'gofferson_2';
    private static $_password = 'MOtley256';
    private static $_username = 'gofferson_2';

    public static function getPDO(){
        try{ 
            $dbh = new PDO("mysql:host=".self::$_host.";dbname=".self::$_dbname, self::$_username, self::$_password);
            $dbh->query('SET NAMES UTF8');
        }catch(PDOException $e) {
            echo $e->getMessage();
        }
        return $dbh;
    }

}
?>
