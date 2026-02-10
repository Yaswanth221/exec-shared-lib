def call(Map cfg = [:]) {
    echo "[gitCheckoutScm] start"
    checkout scm
    echo "[gitCheckoutScm] end"
}
