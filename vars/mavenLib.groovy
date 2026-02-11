def call(Map cfg = [:]) {
  echo "[mavenLib] start"

  // -------------------------
  // Backward compatible mode
  // -------------------------
  if (cfg.goal || cfg.options) {
    def goal = cfg.goal ?: "test"
    def options = cfg.options ?: "-U"

    echo "[mavenLib] mode=simple"
    echo "[mavenLib] goal=${goal}"
    echo "[mavenLib] options=${options}"

    sh "mvn -version"
    sh "mvn ${options} clean ${goal}"

    echo "[mavenLib] end"
    return
  }

  // -------------------------
  // Company recipe mode
  // -------------------------
  echo "[mavenLib] mode=company-recipe"

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

  echo "[mavenLib] branch=${branch}"
  echo "[mavenLib] branchType=${branchType}"
  echo "[mavenLib] goals=clean:${cleanGoal}, main:${mainGoal}"
  echo "[mavenLib] options=${options}"
  echo "[mavenLib] settingsXml=${settingsXml ?: 'none'}"
  echo "[mavenLib] gitUser=${cfg.gitUser ?: 'none'} gitEmail=${cfg.gitEmail ?: 'none'}"
  echo "[mavenLib] sonarUrl=${cfg.sonarUrl ?: 'disabled'}"
  echo "[mavenLib] secretsScan=${cfg.secretsScan}"
  echo "[mavenLib] scaScan=${cfg.scaScan ? 'enabled' : 'disabled'}"

  // simulate scans (so you can "feel" them)
  if (cfg.secretsScan) echo "[mavenLib] secrets scan (simulate) -> OK"
  if (cfg.scaScan)     echo "[mavenLib] SCA scan (simulate) -> OK"

  // actual build
  sh "mvn -version"
  sh "mvn ${options} ${settingsArg} ${cleanGoal} ${mainGoal}"

  // sonar (simulate for now)
  if (cfg.sonarUrl) {
    echo "[mavenLib] sonar enabled (simulate) -> would run: mvn sonar:sonar -Dsonar.host.url=${cfg.sonarUrl}"
    // later, when you have sonar up, uncomment:
    // sh "mvn ${options} ${settingsArg} sonar:sonar -Dsonar.host.url=${cfg.sonarUrl}"
  } else {
    echo "[mavenLib] sonar skipped"
  }

  echo "[mavenLib] end"
}
