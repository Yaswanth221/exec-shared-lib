def call(Map cfg = [:]) {
  echo "[mavenLib] start"

  // simple mode support (your existing)
  if (cfg.goal || cfg.options) {
    def goal = cfg.goal ?: "test"
    def options = cfg.options ?: "-U"
    sh "mvn -version"
    sh "mvn ${options} clean ${goal}"
    echo "[mavenLib] end"
    return
  }

  // company mode
  def branch = env.BRANCH_NAME ?: "local"
  def branchType =
    (branch == "develop") ? "develop" :
    (branch.startsWith("release/") || branch.startsWith("release-") || branch == "release") ? "release" :
    "other"

  def goalsMap = cfg.mavenGoals ?: [release: 'deploy', develop: 'deploy', other: 'install', all: 'clean']
  def cleanGoal = goalsMap.all ?: "clean"
  def mainGoal  = goalsMap[branchType] ?: "install"

  def optionsList = (cfg.mavenOptions?.all instanceof List) ? cfg.mavenOptions.all : ['-U']
  def options = optionsList.join(" ")

  def settingsXml = cfg.settingsXml
  def settingsArg = settingsXml ? "-s ${settingsXml}" : ""

  echo "[mavenLib] branch=${branch} type=${branchType}"
  echo "[mavenLib] mvn: ${cleanGoal} ${mainGoal}"
  echo "[mavenLib] sonarUrl=${cfg.sonarUrl ?: 'disabled'}"

  // Build
  sh "mvn -version"
  sh "mvn ${options} ${settingsArg} ${cleanGoal} ${mainGoal}"

  // Sonar scan + quality gate
  if (cfg.sonarUrl) {
    def sonarServerName = "sonarqube-local"   // must match Jenkins system config

    withSonarQubeEnv(sonarServerName) {
      def projectKey = env.JOB_NAME.replaceAll('/', ':')
      def projectName = env.JOB_NAME

      echo "[mavenLib] Sonar scan start key=${projectKey}"
      sh """
        mvn ${options} ${settingsArg} sonar:sonar \
          -Dsonar.projectKey=${projectKey} \
          -Dsonar.projectName=${projectName}
      """
      echo "[mavenLib] Sonar scan end"
    }

    timeout(time: 10, unit: 'MINUTES') {
      def qg = waitForQualityGate()
      echo "[mavenLib] Quality Gate: ${qg.status}"

      if (cfg.sonarScanOverrides?.failOnQualityGate && qg.status != "OK") {
        error("[mavenLib] Quality Gate failed: ${qg.status}")
      }
    }
  } else {
    echo "[mavenLib] sonar skipped"
  }

  echo "[mavenLib] end"
}
