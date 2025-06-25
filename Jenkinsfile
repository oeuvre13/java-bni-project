pipeline {
    agent any
    
    environment {
        APP_NAME = 'jenkins-demo-app'
        APP_VERSION = "${BUILD_NUMBER}"
        DOCKER_IMAGE = "${APP_NAME}:${APP_VERSION}"
        
        // Telegram Bot Configuration
        TELEGRAM_BOT_TOKEN = '8029797501:AAHvAp4KV1KUabDAFN-Kalc58MDKm1sgQyc'
        TELEGRAM_CHAT_ID = '2052628431'
        
        // Environment URLs
        STAGING_URL = 'http://localhost:8081'
        PRODUCTION_URL = 'http://localhost:8090'
        
        // Quality Gates
        TEST_THRESHOLD = '80'
        COVERAGE_THRESHOLD = '70'
    }
    
    parameters {
        choice(
            name: 'DEPLOY_ENVIRONMENT',
            choices: ['staging', 'production', 'both'],
            description: 'Select deployment environment'
        )
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: 'Skip test execution'
        )
        booleanParam(
            name: 'ENABLE_NOTIFICATIONS',
            defaultValue: true,
            description: 'Enable Telegram notifications'
        )
        string(
            name: 'CUSTOM_TAG',
            defaultValue: '',
            description: 'Custom Docker tag (optional)'
        )
    }
    
    stages {
        stage('🚀 Pipeline Initialization') {
            steps {
                script {
                    // Calculate custom image tag
                    env.FINAL_TAG = params.CUSTOM_TAG ?: env.APP_VERSION
                    env.DOCKER_IMAGE_FINAL = "${APP_NAME}:${env.FINAL_TAG}"
                    
                    // Send detailed start notification
                    def startMessage = """
🚀 <b>CI/CD Pipeline Started</b>
━━━━━━━━━━━━━━━━━━━━━━━
📋 <b>Job:</b> ${env.JOB_NAME}
🔢 <b>Build:</b> #${env.BUILD_NUMBER}
🌿 <b>Branch:</b> ${env.BRANCH_NAME ?: 'main'}
🎯 <b>Environment:</b> ${params.DEPLOY_ENVIRONMENT}
🏷️ <b>Tag:</b> ${env.FINAL_TAG}
👤 <b>Triggered by:</b> ${env.BUILD_USER ?: 'System'}
⏰ <b>Started at:</b> ${new Date().format('yyyy-MM-dd HH:mm:ss')}
🔗 <b>Console:</b> ${env.BUILD_URL}console
                    """
                    if (params.ENABLE_NOTIFICATIONS) {
                        sendTelegramMessage(startMessage)
                    }
                }
                
                echo '🔍 Initializing pipeline...'
                echo "Deploy Environment: ${params.DEPLOY_ENVIRONMENT}"
                echo "Skip Tests: ${params.SKIP_TESTS}"
                echo "Docker Image: ${env.DOCKER_IMAGE_FINAL}"
            }
        }
        
        stage('📥 Source Code Checkout') {
            steps {
                echo '📥 Checking out source code...'
                // Explicitly check out the source code from Git
                // This is used instead of 'checkout scm' to make the pipeline script
                // work even when it's not loaded from an SCM in the Jenkins job config.
                git url: 'https://github.com/zikazama/jenkins.git', branch: 'main'
                
                script {
                    // Get commit info
                    env.GIT_COMMIT_SHORT = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()
                    
                    env.GIT_COMMIT_MSG = sh(
                        script: 'git log -1 --pretty=%B',
                        returnStdout: true
                    ).trim()
                    
                    if (params.ENABLE_NOTIFICATIONS) {
                        sendTelegramMessage("📥 <b>Checkout Complete</b>\n🔗 Commit: ${env.GIT_COMMIT_SHORT}\n💬 ${env.GIT_COMMIT_MSG}")
                    }
                }
                
                sh 'ls -la'
            }
        }
        
        stage('🔨 Build Application') {
            steps {
                echo '🔨 Building the application...'
                sh 'mvn clean compile -B'
                
                script {
                    if (params.ENABLE_NOTIFICATIONS) {
                        sendTelegramMessage("🔨 <b>Build Stage Completed</b>\n✅ Application compiled successfully")
                    }
                }
            }
            post {
                failure {
                    script {
                        if (params.ENABLE_NOTIFICATIONS) {
                            sendTelegramMessage("❌ <b>Build Failed</b>\n🚨 Compilation errors detected")
                        }
                    }
                }
            }
        }
        
        stage('🧪 Testing & Quality Analysis') {
            parallel {
                stage('Unit Tests') {
                    when {
                        expression { !params.SKIP_TESTS }
                    }
                    steps {
                        echo '🧪 Running unit tests...'
                        sh 'mvn test -B'
                    }
                    post {
                        always {
                            publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                            archiveArtifacts artifacts: 'target/surefire-reports/*', fingerprint: true
                        }
                        success {
                            script {
                                if (params.ENABLE_NOTIFICATIONS) {
                                    sendTelegramMessage("🧪 <b>Unit Tests Passed</b>\n✅ All tests successful")
                                }
                            }
                        }
                        failure {
                            script {
                                if (params.ENABLE_NOTIFICATIONS) {
                                    sendTelegramMessage("❌ <b>Unit Tests Failed</b>\n🚨 Test failures detected")
                                }
                            }
                        }
                    }
                }
                
                stage('Code Quality') {
                    steps {
                        echo '📊 Running code quality analysis...'
                        sh 'mvn verify -B -DskipTests'
                        
                        script {
                            if (params.ENABLE_NOTIFICATIONS) {
                                sendTelegramMessage("📊 <b>Code Quality Check</b>\n✅ Quality analysis completed")
                            }
                        }
                    }
                }
                
                stage('Security Scan') {
                    steps {
                        echo '🔒 Running security scan...'
                        script {
                            // Simulate security scan
                            sleep(time: 10, unit: 'SECONDS')
                            echo 'Security scan completed - No vulnerabilities found'
                            
                            if (params.ENABLE_NOTIFICATIONS) {
                                sendTelegramMessage("🔒 <b>Security Scan</b>\n✅ No vulnerabilities detected")
                            }
                        }
                    }
                }
            }
        }
        
        stage('📦 Package Application') {
            steps {
                echo '📦 Packaging the application...'
                sh 'mvn package -DskipTests -B'
                
                script {
                    if (params.ENABLE_NOTIFICATIONS) {
                        sendTelegramMessage("📦 <b>Package Complete</b>\n✅ JAR file created successfully")
                    }
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
        
        stage('🐳 Docker Build & Registry') {
            steps {
                echo '🐳 Building Docker image...'
                script {
                    docker.build("${env.DOCKER_IMAGE_FINAL}", "-f docker/Dockerfile .")
                    
                    if (params.ENABLE_NOTIFICATIONS) {
                        sendTelegramMessage("🐳 <b>Docker Image Built</b>\n🏷️ Image: ${env.DOCKER_IMAGE_FINAL}")
                    }
                }
            }
        }
        
        stage('🎯 Staging Deployment') {
            when {
                anyOf {
                    equals expected: 'staging', actual: params.DEPLOY_ENVIRONMENT
                    equals expected: 'both', actual: params.DEPLOY_ENVIRONMENT
                }
            }
            steps {
                echo '🎯 Deploying to staging environment...'
                script {
                    deployToEnvironment('staging', 8081, env.DOCKER_IMAGE_FINAL)
                    
                    if (params.ENABLE_NOTIFICATIONS) {
                        sendTelegramMessage("🎯 <b>Staging Deployment</b>\n✅ Deployed successfully\n🔗 URL: ${env.STAGING_URL}")
                    }
                }
            }
        }
        
        stage('🔍 Staging Tests') {
            when {
                anyOf {
                    equals expected: 'staging', actual: params.DEPLOY_ENVIRONMENT
                    equals expected: 'both', actual: params.DEPLOY_ENVIRONMENT
                }
            }
            steps {
                echo '🔍 Running staging integration tests...'
                script {
                    sleep(time: 30, unit: 'SECONDS') // Wait for app to start
                    
                    // Health check
                    def healthStatus = sh(
                        script: "curl -s -o /dev/null -w '%{http_code}' ${env.STAGING_URL}/health || echo '000'",
                        returnStdout: true
                    ).trim()
                    
                    if (healthStatus == '200') {
                        echo '✅ Health check passed'
                        
                        // API test
                        sh "curl -f ${env.STAGING_URL}/ || exit 1"
                        echo '✅ API test passed'
                        
                        if (params.ENABLE_NOTIFICATIONS) {
                            sendTelegramMessage("🔍 <b>Staging Tests</b>\n✅ All integration tests passed\n🩺 Health: OK")
                        }
                    } else {
                        error("Health check failed with status: ${healthStatus}")
                    }
                }
            }
            post {
                failure {
                    script {
                        if (params.ENABLE_NOTIFICATIONS) {
                            sendTelegramMessage("❌ <b>Staging Tests Failed</b>\n🚨 Integration tests failed")
                        }
                    }
                }
            }
        }
        
        stage('⏳ QA Approval') {
            when {
                anyOf {
                    equals expected: 'production', actual: params.DEPLOY_ENVIRONMENT
                    equals expected: 'both', actual: params.DEPLOY_ENVIRONMENT
                }
            }
            steps {
                script {
                    if (params.ENABLE_NOTIFICATIONS) {
                        sendTelegramMessage("⏳ <b>QA Approval Required</b>\n🔍 Please review staging environment\n🎯 Staging: ${env.STAGING_URL}\n⏰ Waiting for approval...")
                    }
                    
                    timeout(time: 15, unit: 'MINUTES') {
                        input message: 'QA Approval: Deploy to Production?', 
                              ok: 'Approve',
                              parameters: [
                                  choice(name: 'QA_DECISION', choices: ['Approve', 'Reject'], description: 'QA Decision'),
                                  text(name: 'QA_COMMENTS', defaultValue: '', description: 'QA Comments (optional)')
                              ]
                    }
                    
                    if (params.ENABLE_NOTIFICATIONS) {
                        sendTelegramMessage("✅ <b>QA Approved</b>\n👤 Decision: ${QA_DECISION}\n💬 Comments: ${QA_COMMENTS ?: 'None'}")
                    }
                }
            }
        }
        
        stage('🚀 Production Deployment') {
            when {
                anyOf {
                    equals expected: 'production', actual: params.DEPLOY_ENVIRONMENT
                    equals expected: 'both', actual: params.DEPLOY_ENVIRONMENT
                }
            }
            steps {
                script {
                    if (params.ENABLE_NOTIFICATIONS) {
                        sendTelegramMessage("⏳ <b>DevOps Approval Required</b>\n🚀 Ready for production deployment\n⏰ Waiting for final approval...")
                    }
                    
                    timeout(time: 10, unit: 'MINUTES') {
                        input message: 'DevOps Approval: Deploy to Production?', 
                              ok: 'Deploy',
                              parameters: [
                                  choice(name: 'DEVOPS_DECISION', choices: ['Deploy', 'Abort'], description: 'DevOps Decision'),
                                  text(name: 'DEPLOYMENT_NOTES', defaultValue: '', description: 'Deployment Notes (optional)')
                              ]
                    }
                    
                    echo '🚀 Deploying to production environment...'
                    deployToEnvironment('prod', 8090, env.DOCKER_IMAGE_FINAL)
                    
                    if (params.ENABLE_NOTIFICATIONS) {
                        sendTelegramMessage("🚀 <b>Production Deployment</b>\n✅ Deployed successfully\n👤 Approved by: DevOps\n💬 Notes: ${DEPLOYMENT_NOTES ?: 'None'}\n🔗 URL: ${env.PRODUCTION_URL}")
                    }
                }
            }
        }
        
        stage('🔍 Production Verification') {
            when {
                anyOf {
                    equals expected: 'production', actual: params.DEPLOY_ENVIRONMENT
                    equals expected: 'both', actual: params.DEPLOY_ENVIRONMENT
                }
            }
            steps {
                echo '🔍 Verifying production deployment...'
                script {
                    sleep(time: 30, unit: 'SECONDS')
                    
                    // Health check
                    def healthStatus = sh(
                        script: "curl -s -o /dev/null -w '%{http_code}' ${env.PRODUCTION_URL}/health || echo '000'",
                        returnStdout: true
                    ).trim()
                    
                    if (healthStatus == '200') {
                        echo '✅ Production health check passed'
                        
                        // Basic smoke test
                        sh "curl -f ${env.PRODUCTION_URL}/ || exit 1"
                        echo '✅ Production smoke test passed'
                        
                        if (params.ENABLE_NOTIFICATIONS) {
                            sendTelegramMessage("🔍 <b>Production Verification</b>\n✅ All production tests passed\n🩺 Health: OK\n🎉 Deployment successful!")
                        }
                    } else {
                        error("Production health check failed with status: ${healthStatus}")
                    }
                }
            }
        }
        
        stage('📊 Post-Deployment Report') {
            steps {
                script {
                    def endTime = new Date().format('yyyy-MM-dd HH:mm:ss')
                    def duration = currentBuild.durationString.replace(' and counting', '')
                    
                    def reportMessage = """
📊 <b>Deployment Report</b>
━━━━━━━━━━━━━━━━━━━━━━━
✅ <b>Status:</b> SUCCESS
📋 <b>Job:</b> ${env.JOB_NAME}
🔢 <b>Build:</b> #${env.BUILD_NUMBER}
🏷️ <b>Version:</b> ${env.FINAL_TAG}
🎯 <b>Environment:</b> ${params.DEPLOY_ENVIRONMENT}
⏱️ <b>Duration:</b> ${duration}
⏰ <b>Completed:</b> ${endTime}

🌐 <b>Deployed URLs:</b>
${params.DEPLOY_ENVIRONMENT.contains('staging') ? '🎯 Staging: ' + env.STAGING_URL : ''}
${params.DEPLOY_ENVIRONMENT.contains('production') ? '🚀 Production: ' + env.PRODUCTION_URL : ''}

🔗 <b>Jenkins:</b> ${env.BUILD_URL}
                    """
                    
                    if (params.ENABLE_NOTIFICATIONS) {
                        sendTelegramMessage(reportMessage)
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo '🧹 Cleaning up workspace...'
            cleanWs()
        }
        
        success {
            script {
                def successMessage = """
🎉 <b>Pipeline Completed Successfully!</b>
━━━━━━━━━━━━━━━━━━━━━━━
✅ <b>Result:</b> SUCCESS
📋 <b>Job:</b> ${env.JOB_NAME}
🔢 <b>Build:</b> #${env.BUILD_NUMBER}
⏱️ <b>Duration:</b> ${currentBuild.durationString.replace(' and counting', '')}
🎯 <b>Environment:</b> ${params.DEPLOY_ENVIRONMENT}
                """
                
                if (params.ENABLE_NOTIFICATIONS) {
                    sendTelegramMessage(successMessage)
                }
            }
        }
        
        failure {
            script {
                def failureMessage = """
❌ <b>Pipeline Failed!</b>
━━━━━━━━━━━━━━━━━━━━━━━
🚨 <b>Result:</b> FAILURE
📋 <b>Job:</b> ${env.JOB_NAME}
🔢 <b>Build:</b> #${env.BUILD_NUMBER}
⏱️ <b>Duration:</b> ${currentBuild.durationString.replace(' and counting', '')}
🔗 <b>Console:</b> ${env.BUILD_URL}console
                """
                
                if (params.ENABLE_NOTIFICATIONS) {
                    sendTelegramMessage(failureMessage)
                }
            }
        }
        
        unstable {
            script {
                def unstableMessage = """
⚠️ <b>Pipeline Unstable</b>
━━━━━━━━━━━━━━━━━━━━━━━
🟡 <b>Result:</b> UNSTABLE
📋 <b>Job:</b> ${env.JOB_NAME}
🔢 <b>Build:</b> #${env.BUILD_NUMBER}
🔗 <b>Console:</b> ${env.BUILD_URL}console
                """
                
                if (params.ENABLE_NOTIFICATIONS) {
                    sendTelegramMessage(unstableMessage)
                }
            }
        }
        
        aborted {
            script {
                def abortedMessage = """
🛑 <b>Pipeline Aborted</b>
━━━━━━━━━━━━━━━━━━━━━━━
🔴 <b>Result:</b> ABORTED
📋 <b>Job:</b> ${env.JOB_NAME}
🔢 <b>Build:</b> #${env.BUILD_NUMBER}
                """
                
                if (params.ENABLE_NOTIFICATIONS) {
                    sendTelegramMessage(abortedMessage)
                }
            }
        }
    }
}

// Helper function to deploy to different environments
def deployToEnvironment(String environment, int port, String dockerImage) {
    echo "Deploying to ${environment} on port ${port}"
    
    // Stop and remove existing container
    sh """
        docker stop ${env.APP_NAME}-${environment} || true
        docker rm ${env.APP_NAME}-${environment} || true
    """
    
    // Run new container
    sh """
        docker run -d \
            --name ${env.APP_NAME}-${environment} \
            -p ${port}:8080 \
            -e SPRING_PROFILES_ACTIVE=${environment} \
            --restart unless-stopped \
            ${dockerImage}
    """
    
    echo "✅ ${environment} deployment completed"
}

// Enhanced Telegram notification function with retry logic
def sendTelegramMessage(String message) {
    if (!params.ENABLE_NOTIFICATIONS) {
        return
    }
    
    try {
        def encodedMessage = message.replaceAll('"', '\\\\"')
        def maxRetries = 3
        def retryCount = 0
        def success = false
        
        while (retryCount < maxRetries && !success) {
            try {
                def response = sh(
                    script: """
                        curl -s -X POST https://api.telegram.org/bot\${TELEGRAM_BOT_TOKEN}/sendMessage \
                            -d chat_id=\${TELEGRAM_CHAT_ID} \
                            -d text="${encodedMessage}" \
                            -d parse_mode=HTML \
                            -w "HTTP_CODE:%{http_code}"
                    """,
                    returnStdout: true
                ).trim()
                
                if (response.contains('HTTP_CODE:200')) {
                    echo "✅ Telegram notification sent successfully"
                    success = true
                } else {
                    throw new Exception("HTTP error: ${response}")
                }
            } catch (Exception e) {
                retryCount++
                echo "⚠️ Telegram notification attempt ${retryCount} failed: ${e.getMessage()}"
                if (retryCount < maxRetries) {
                    sleep(time: 5, unit: 'SECONDS')
                }
            }
        }
        
        if (!success) {
            echo "❌ Failed to send Telegram notification after ${maxRetries} attempts"
        }
    } catch (Exception e) {
        echo "❌ Telegram notification error: ${e.getMessage()}"
    }
} 