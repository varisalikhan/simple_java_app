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

              - name: snyk
                image: snyk/snyk:docker
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

    environment {
        SNYK_TOKEN = credentials('snyk-api-token')  // Reference the Jenkins secret
    }

    stages {
        stage('Check Podman') {
            steps {
                container('podman') {
                    sh 'podman --version'
                }
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew clean build'
            }
        }

        stage('Podman Build') {
            steps {
                container('podman') {
                    sh 'podman build -t daundkarash/java-application2_local .'
                    sh 'podman save -o /var/lib/containers/java-application2_local.tar daundkarash/java-application2_local'
                }
            }
        }

        stage('Load Image') {
            steps {
                container('podman') {
                    sh 'podman load -i /var/lib/containers/java-application2_local.tar'
                }
            }
        }

        stage('Verify Image') {
            steps {
                container('podman') {
                    sh 'podman images | grep daundkarash/java-application2_local'
                }
            }
        }

        stage('Snyk Container Scan') {
            steps {
                container('snyk') {
                    script {
                        catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                            sh 'snyk auth $SNYK_TOKEN'  // Authenticate with Snyk 
                            sh 'snyk monitor --docker docker-archive:/var/lib/containers/java-application2_local.tar --file=Dockerfile --json --debug > snyk_scan_results.json'
                        }
                            // sh 'snyk container test docker-archive:/var/lib/containers/java-application2_local.tar --file=Dockerfile --json --debug > snyk_scan_results.json'
                            // sh 'snyk monitor --org=b250d181-3d61-47e9-8bfb-aa1375a534cc --json --debug >> snyk_scan_results.json'  // Monitor command to send the report to Snyk.io
                        }
                    }
                }
            }
        }

        stage('Archive Snyk Results') {
            steps {
                archiveArtifacts artifacts: 'snyk_scan_results.json', allowEmptyArchive: true
            }
        }

        // stage('Push Image to GitLab') {
        //     steps {
        //         container('podman') {
        //             script {
        //                 withCredentials([usernamePassword(credentialsId: 'gitlab-registry', usernameVariable: 'GITLAB_USER', passwordVariable: 'GITLAB_TOKEN')]) {
        //                     sh 'podman login registry.gitlab.com -u ${GITLAB_USER} -p ${GITLAB_TOKEN}'
        //                     sh 'podman tag daundkarash/java-application2_local registry.gitlab.com/test8011231/jenkins-image-push/java-application2_local:latest'
        //                     sh 'podman push registry.gitlab.com/test8011231/jenkins-image-push/java-application2_local:latest'
        //                 }
        //             }
        //         }
        //     }
        // }
    }
}
