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
                    sh 'podman inspect localhost/daundkarash/java-application2_local:latest'
                }
            }
        }

        stage('Snyk Container Scan') {
            steps {
                // Ensure Snyk plugin is installed and configured
                snykSecurity(
                    snykInstallation: 'default',  // Replace with your configured Snyk installation name
                    snykTokenId: 'snyk-api-token',  // Referencing the Snyk API token credential ID
                    failOnIssues: true,
                    failOnError: true,
                    monitorProjectOnBuild: false,
                    organization: '', // Optional, specify if you have an organization
                    projectName: 'java-application2_local',
                    targetFile: '/var/lib/containers/Dockerfile', // Specify the Dockerfile if needed
                    severity: 'high',
                    additionalArguments: '--json --debug', // Additional arguments if needed
                    // Provide the path to the tar file if Snyk plugin supports it directly
                    // Check documentation if specific parameters are needed for tar files
                )
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
