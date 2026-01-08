pipeline {
    agent any

    environment {
        
        SMTP_USER = credentials('smtp-user')
        SMTP_PASS = credentials('smtp-pass')
    }

    options {
        timestamps()
        ansiColor('xterm')
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/Pranitha161/Smart-Ticket-Issue-Management-System.git'
            }
        }

        stage('Build Microservices') {
            steps {
                script {
                    def services = [
                        "Backend/auth-user-service",
                        "Backend/ticket-service",
                        "Backend/assignment-escalation-service",
                        "Backend/notification-service",
                        "Backend/dashboard-service",
                        "Backend/smartticket-service-registry",
                        "Backend/smartticket-config-server",
                        "Backend/smartticket-api-gateway"
                    ]
                    for (svc in services) {
                        sh """
                            echo "Building ${svc}"
                            cd ${svc}
                            ./mvnw  package -DskipTests
                        """
                    }
                }
            }
        }

        stage('Generate .env') {
            steps {
                sh '''
                set -e
                cat > .env <<EOF
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
KAFKA_GROUP_ID=smart-ticket-service-group

SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=${SMTP_USER}
SMTP_PASSWORD=${SMTP_PASS}
SMTP_PROTOCOL=smtp

SPRING_PROFILES_ACTIVE=docker
EOF
                echo "Generated .env (secrets not printed)"
                '''
            }
        }

        stage('Build Docker Images') {
            steps {
                sh 'docker-compose --env-file .env build'
            }
        }

        stage('Deploy (Optional)') {
            when { expression { return env.DEPLOY?.toBoolean() } }
            steps {
                sh '''
                docker-compose --env-file .env up -d
                docker-compose ps
                '''
            }
        }
    }

    post {
        always {
            sh 'docker-compose logs --tail=100 || true'
        }
    }
}
