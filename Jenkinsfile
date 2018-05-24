pipeline {

    agent {
        label 'docker'
    }

    options {
        timestamps()
        buildDiscarder logRotator(artifactDaysToKeepStr: '14', artifactNumToKeepStr: '10', daysToKeepStr: '30', numToKeepStr: '20')
    }

    tools {
        jdk 'JDK8'
        maven 'Maven 3.5.x'
    }

    environment {
        MAVEN_OPTS = '-Xmx1024m -Xms256m -XX:MaxPermSize=256m'
    }

    stages{
        stage('Environment Setup Validation') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                    java -version
                    javac -version
                    mvn --version
                '''
            }
        }
        stage("Compile (Only PR)") {
            when {
                changeRequest()
                beforeAgent true
            }
            steps{
                mvn("clean package -DskipTests -DskipITs -Dinvoker.skip=true")
            }
        }
        stage("Build") {
            environment {
                mvnGoal = "clean install"
            }
            steps{
                script {
                    if (env.BRANCH_NAME.contains('.x') || env.BRANCH_NAME.equals('master')) {
                        mvnGoal = "clean deploy"
                    }
                }
                echo mvnGoal
                mvn(mvnGoal)
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
        }
        cleanup {
            cleanWs()
        }
    }
}

def mvn(String buildCommandLine = 'clean install', String mavenSettingsFileId = 'mule-runtime-maven-settings-MuleSettings') {

    configFileProvider([configFile(fileId: mavenSettingsFileId, variable: 'mavenSettingsFilePath')]) {
        sh "mvn --settings '${mavenSettingsFilePath}' ${buildCommandLine} -Dmaven.test.failure.ignore=true"
    }

}

//node('docker') {
//
//    stage('Install Tools') {
//        installJava()
//        sh 'java -version'
//        sh 'javac -version'
//
//        installMaven()
//        sh 'mvn --version'
//    }
//
//    workspaceLocation = pwd()
//    repositoryLocation = workspaceLocation + '/.repository'
//
//    stage('Clone Repo') {
//        git branch: 'master', credentialsId: 'gittoken', url: 'git@github.com:mulesoft/mule-xml-module.git'
//    }
//
//    mvn_command = "clean install"
//    stage('Set Maven Properties') {
//
//        // if (env.BRANCH_NAME.contains('.x') || env.BRANCH_NAME.equals('master')) {
//        //   mvn_command = "clean deploy"
//        // }
//
//    }
//
//    stage('Build') {
//        configFileProvider([configFile(fileId: 'mule-runtime-maven-settings-MuleSettings', variable: 'org.mule.maven.client.api.SettingsSupplierFactory.userSettings')]) {
//           mvn("${mvn_command}")
//        }
//
//    }
//}

//def mvn(command) {
//    def mavenToolName = 'M3.5'
//    def mavenSettingsConfig = 'mule-runtime-maven-settings-MuleSettings'
//
//    withMaven(maven: mavenToolName,
//            mavenSettingsConfig: mavenSettingsConfig,
//            mavenLocalRepo: repositoryLocation,
//            options: [
//                    findbugsPublisher(disabled: true),
//                    artifactsPublisher(disabled: true),
//                    openTasksPublisher(disabled: true)]) {
//        if (isUnix()) {
//            sh "mvn ${command}"
//        } else {
//            bat(/mvn ${command}/)
//        }
//    }
//}

//def installJava(){
//    def jdkToolName = 'JDK8'
//    env.JAVA_HOME = tool jdkToolName
//    env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
//    sh 'java -version'
//}
//
//def installMaven(){
//    env.M2_HOME = tool 'M3.5'
//    env.PATH = "${env.M2_HOME}/bin:${env.PATH}"
//    sh 'mvn --version'
//}
