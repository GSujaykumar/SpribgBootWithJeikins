pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'obitouchia/springbootwithjenkins'
        DOCKER_TAG = 'latest'
        GITHUB_REPO = 'https://github.com/GSujaykumar/SpribgBootWithJeikins.git'
    }

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: "${GITHUB_REPO}"
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean install -Pjenkins'
            }
        }

        stage('Verify JAR') {
            steps {
                sh 'ls -la target/springboot-mysql-api.jar'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -f Dockerfile.Ci -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                    sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                }
            }
        }

        stage('Deploy') {
            steps {
                sh 'docker-compose -f docker-compose.ci.yml pull app'
                sh 'docker-compose -f docker-compose.ci.yml up -d'
            }
        }

        stage('Health Check') {
            steps {
                sh '''
                    sleep 25
                    curl -f http://localhost:8080/api/health || exit 1
                '''
            }
        }
    }

    post {
        success {
            junit '**/target/surefire-reports/TEST-*.xml'
            archiveArtifacts artifacts: 'target/springboot-mysql-api.jar', fingerprint: true
            echo 'Pipeline succeeded! API: http://localhost:8080/api/health'
        }
        failure {
            echo 'Pipeline failed! Check Jenkins console output.'
        }
        always {
            sh 'docker-compose -f docker-compose.ci.yml ps || true'
        }
    }
}
