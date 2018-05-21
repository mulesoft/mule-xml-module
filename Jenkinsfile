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
      }
    }
  }
}