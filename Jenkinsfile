pipeline {
  agent {
    node {
      label 'docker'
    }
    
  }
  stages {
    stage('Build') {
      steps {
        tool(name: 'JDK8', type: 'jdk')
        tool(name: 'M3.5', type: 'maven')
        sh 'mvn --version'
      }
    }
  }
}