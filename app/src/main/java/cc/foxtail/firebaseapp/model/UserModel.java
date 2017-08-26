package cc.foxtail.firebaseapp.model;

public class UserModel {
    private String email;

    public void setEmail(String email) {
        this.email = email;
    }

    private static class Singleton {
        private static final UserModel INSTANCE = new UserModel();
    }

    public static UserModel getInstance() {
        return Singleton.INSTANCE;
    }
}
