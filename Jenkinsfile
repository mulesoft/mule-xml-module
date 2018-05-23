node('docker') {

    stage('Install Tools') {
        tool(name: 'JDK8', type: 'jdk')
        sh 'java --version'
        sh 'javac --version'

        tool(name: 'M3.5', type: 'maven')
        sh 'mvn --version'
    }

    stage('Build') {
        configFileProvider([configFile(fileId: 'org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig1422369625996', variable: 'org.mule.maven.client.api.SettingsSupplierFactory.userSettings')]) {
            mvn("${mvn_test_args}")
        }
    }
}

def mvn(command) {
    def mavenToolName = 'Maven 3.3.9'
    def mavenSettingsConfig = 'org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig1422369625996'

    if ("${dry_run_param}".toBoolean()) {
        mavenSettingsConfig = 'c8837f87-5fcb-4f8d-8fbd-03c6925976f5'
    }

    def globalMavenSettingsConfig = 'org.jenkinsci.plugins.configfiles.maven.GlobalMavenSettingsConfig1422306165153'
    withMaven(maven: mavenToolName,
            mavenSettingsConfig: mavenSettingsConfig,
            globalMavenSettingsConfig: globalMavenSettingsConfig,
            mavenLocalRepo: repositoryLocation,
            options: [
                    findbugsPublisher(disabled: true),
                    artifactsPublisher(disabled: true),
                    openTasksPublisher(disabled: true)]) {
        if (isUnix()) {
            sh "mvn ${command}"
        } else {
            bat(/mvn ${command}/)
        }
    }
}

def installJava(){
    def jdkToolName = 'Oracle JDK 8u152'
    env.JAVA_HOME = tool jdkToolName
    env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
    sh 'java -version'
}

def installMaven(){
    env.M2_HOME = tool 'Maven 3.5.2'
    env.PATH = "${env.M2_HOME}/bin:${env.PATH}"
    sh 'mvn --version'
}
