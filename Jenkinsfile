 pipeline {
  agent any

  stages {
        stage('Build') {
            steps {
                sh 'mvn clean compile assembly:single'
            }
        }
        stage('Deploy') {
            steps {
                sh 'cp target/scheduler.jar /data/trailtour/scheduler/'
            }
        }
    }
}
