package cc.foxtail.firebaseapp.model;

public interface OnUploadImageListener {
    void onSuccess(String url);
    void onFail();
}
