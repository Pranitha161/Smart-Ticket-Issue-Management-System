pipeline {
    agent any

    environment {
        SMTP_USER = credentials('smtp-user')
        SMTP_PASS = credentials('smtp-pass')
    }

    options {
        timestamps()
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
                        bat """
                            echo Building ${svc}
                            cd ${svc}
                            mvnw.cmd package -DskipTests
                        """
                    }
                }
            }
        }

        stage('Generate .env') {
            steps {
                dir('Backend') {
                    bat """
                        echo KAFKA_BOOTSTRAP_SERVERS=kafka:9092 > .env
                        echo KAFKA_GROUP_ID=smart-ticket-service-group >> .env
                        echo SMTP_HOST=smtp.gmail.com >> .env
                        echo SMTP_PORT=587 >> .env
                        echo SMTP_USERNAME=%SMTP_USER% >> .env
                        echo SMTP_PASSWORD=%SMTP_PASS% >> .env
                        echo SMTP_PROTOCOL=smtp >> .env
                        echo SPRING_PROFILES_ACTIVE=docker >> .env
                        echo Generated Backend\\.env (secrets not printed)
                    """
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                dir('Backend') {
                    bat "docker-compose --env-file .env build"
                }
            }
        }

        stage('Deploy (Optional)') {
            steps {
                dir('Backend') {
                    bat "docker-compose --env-file .env up -d"
                    bat "docker-compose ps"
                }
            }
        }
    }

    post {
        always {
            dir('Backend') {
                bat "docker-compose logs --tail=100 || exit 0"
            }
        }
        cleanup {
            dir('Backend') {
                bat "del .env || exit 0"
                echo "Cleaned up Backend/.env after build"
            }
        }
    }
}
