def call(Map cfg = [:]) {
    echo "[mavenLib] start"

    def goal = cfg.goal ?: "test"
    def options = cfg.options ?: "-U"

    echo "[mavenLib] goal=${goal}"
    echo "[mavenLib] options=${options}"

    sh "mvn -version"
    sh "mvn ${options} clean ${goal}"

    echo "[mavenLib] end"
}
