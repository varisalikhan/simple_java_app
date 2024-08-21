pipeline {
    agent {
        kubernetes {
            yaml """
            apiVersion: v1
            kind: Pod
            spec:
              containers:
              - name: podman
                image: quay.io/podman/stable:latest
                securityContext:
                  privileged: true
                command:
                - sleep
                args:
                - infinity
                volumeMounts:
                - name: podman-graph-storage
                  mountPath: /var/lib/containers

              - name: trivy
                image: aquasec/trivy:latest
                command:
                - sleep
                args:
                - infinity
                volumeMounts:
                - name: podman-graph-storage
                  mountPath: /var/lib/containers

              volumes:
              - name: podman-graph-storage
                emptyDir: {}
            """
        }
    }

    stages {
        stage('Check Podman') {
            steps {
                container('podman') {
                    sh 'podman --version'
                }
            }
        } // Check Podman

        stage('Build') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew clean build'
                sh 'ls -l build/libs/'
            }
        } // Build

        stage('Podman Build') {
            steps {
                container('podman') {
                    sh 'podman build -t daundkarash/java-application2_local .'
                }
            }
        } // Podman Build

        stage('Save Image') {
            steps {
                container('podman') {
                    sh 'podman save -o /var/lib/containers/java-application2_local.tar daundkarash/java-application2_local'
                }
            }
        } // Save Image

stage('Container Scanning') {
    steps {
        container('trivy') {
            sh '''
                trivy image --input /var/lib/containers/java-application2_local.tar --format json --output trivy-report.json
                # Optionally, convert JSON to HTML in a simpler way
                echo "<html><body><pre>" > trivy-report.html
                cat trivy-report.json >> trivy-report.html
                echo "</pre></body></html>" >> trivy-report.html
            '''
        }
    }
}

        stage('Push Image to GitLab') {
            steps {
                container('podman') {
                    script {
                        withCredentials([usernamePassword(credentialsId: 'gitlab-registry', usernameVariable: 'GITLAB_USER', passwordVariable: 'GITLAB_TOKEN')]) {
                            sh 'podman login registry.gitlab.com -u ${GITLAB_USER} -p ${GITLAB_TOKEN}'
                            sh 'podman tag daundkarash/java-application2_local registry.gitlab.com/test8011231/jenkins-image-push/java-application2_local:latest'
                            sh 'podman push registry.gitlab.com/test8011231/jenkins-image-push/java-application2_local:latest'
                        }
                    }
                }
            }
        } // Push Image to GitLab
    }

   post {
    always {
        publishHTML([
            allowMissing: false,
            reportName: 'Trivy Scan Results',      // Title in Jenkins UI
            reportDir: '',                         // Directory where the HTML report is saved
            reportFiles: 'trivy-report.html',      // HTML file to publish
            keepAll: true,                         // Keep reports for all builds
            alwaysLinkToLastBuild: true            // Link to the latest build report
        ])
    }
}


}
