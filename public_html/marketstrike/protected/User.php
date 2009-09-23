<?php
Prado::using('System.Security.TDbUserManager');

class User extends TDbUser {

    public function createUser($username) {


        $userRecord=UserRecord::finder()->findByLogin($username);

        if($userRecord instanceof UserRecord) {
            $user=new User($this->Manager);
            $user->Name=$username;

            switch($userRecord->role) {
                case 0: $role = 'user';
                    break;
                case 1: $role = 'admin';
                    break;
                case 2: $role = 'moderator1';
                    break;
                case -1: {
                        $role = 'guest';
                        return null;
                    };
                    break;
                default: $role = 'guest';
            }

            $user->setRoles($role);
            $user->IsGuest=false;

            $_SESSION['User_Id'] = $userRecord->id;

                /* Points for first on day login */



            return $user;
        }
        else
            return null;
    }

    public function validateUser($username,$password) {
        $password = $this->hashPassword($password);
        return UserRecord::finder()->findByLoginAndPassword($username,$password)!==NULL;
    }

    public function hashPassword($pass) {
        return md5('moj-uniwerystetSaltPassword'.$pass);
    }

    public function getUserRecord() {
    //if(!$this->getIsGuest()){
    // $user_id = $this->_id;

        $username=$this->User->Name;
        //die($username);
        $userRecord=UserRecord::finder()->findByLogin($username);

        if($userRecord instanceof UserRecord) {
        //zapisujemy aktywność użytkownika
            $userRecord->last_activity = time();
            $userRecord->save();
            return $userRecord;
        }

        return FALSE;
    }

    public function getUserID() {
        $user = User::getUserRecord();
        if ($user) return $user->id;
    }

    public function getUserRole() {
        $user = User::getUserRecord();
        return $user->role;
    }

    public function IsAdmin() {
        $user = User::getUserRecord();
        return ($user->role == 1);
    }
    public function IsUniversityModertor($university_id) {
        $rel = University_has_moderatorRecord::finder()->findByUniversity_idAndUser_id($university_id,User::getUserID());
        if($rel instanceof University_has_moderatorRecord) {
            return true;
        }
        return false;
    }

    public function addPoints($amount) {
        $user = User::getUserRecord();

        if($user->role == 0) {
            $user->points = $user->points + $amount;
            $user->save();
        }
    }
    public function hasSpecialization($specialization_id) {
        $user = User::getUserRecord();
        $sql = 'SELECT * FROM user_has_specialization WHERE ( user_id = '.$user->id.' and specialization_id = '.$specialization_id.' ) LIMIT 1 ';
        if(is_null(User_has_specializationRecord::finder()->findBySql($sql)))
            return false;
        else
            return true;
    }
    public function hasDepartment($department_id) {
        $user = User::getUserRecord();
        $sql = 'SELECT us.* FROM user_has_specialization us LEFT JOIN university_specialization s ON us.specialization_id = s.id
            WHERE ( us.user_id = '.$user->id.' and s.department_id = '.$department_id.' ) LIMIT 1 ';
        if(is_null(User_has_specializationRecord::finder()->findBySql($sql)))
            return false;
        else
            return true;
    }
    public function hasUniversity($university_id) {
        $user = User::getUserRecord();
        $sql = 'SELECT us.* FROM user_has_specialization us LEFT JOIN university_specialization s ON us.specialization_id = s.id
            LEFT JOIN university_department d ON s.department_id = d.id
            WHERE ( us.user_id = '.$user->id.' and d.university_id = '.$university_id.' ) LIMIT 1 ';
        if(is_null(User_has_specializationRecord::finder()->findBySql($sql)))
            return false;
        else
            return true;
    }
    public function hasFriend($friend_id) {
        $user = User::getUserRecord();
        $sql = 'SELECT * FROM `user_has_user` WHERE ( owner_id = '.$user->id.' and friend_id = '.$friend_id.' ) OR ( owner_id = '.$friend_id.' and friend_id = '.$user->id.' ) LIMIT 0, 30 ';
        if(is_null(User_has_userRecord::finder()->findBySql($sql)))
            return false;
        else
            return true;
    }

    public function foundGroup($group_id) {
        $user = User::getUserRecord();
        $sql = 'SELECT * FROM `group` WHERE ( founder_id = '.$user->id.' and id = '.$group_id.' ) LIMIT 0, 30 ';
        if(is_null(GroupRecord::finder()->findBySql($sql)))
            return false;
        else
            return true;
    }

    static public function sendRecall($id,$msg=null) {

    }

    public function getBirthdate($pesel) {
        $year=substr($pesel,0,2);
        $month=substr($pesel,2,2);
        $day=substr($pesel,4,2);
        if($year>20)
            $year=$year+1900;
        else
            $year=$year+2000;

        return $year.'-'.$month.'-'.$day;
    }


}
    ?>