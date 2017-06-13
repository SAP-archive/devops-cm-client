stage('Check change Status') {
    node {
        step([$class: 'StepChangeCheckExists', ChangeID: params.CHANGE_ID])
        step([$class: 'StepChangeCheckStatus', ChangeID: params.CHANGE_ID, ChangeStatus: 'E0002'])
    }
}

stage('Set development transport') {
    node {
        step([$class: 'StepChangeGetDevelopmentTransport', ChangeID: params.CHANGE_ID])
    }
}

stage('Development'){
    node {
        bat 'echo Hello World ! > test_file.txt'
    }
}

stage('Upload to transport'){
    node {
        step([$class: 'StepUploadFileToTransport', ApplicationID: 'HCP', ChangeID: params.CHANGE_ID, FilePath: 'test_file.txt'])
    }
}

stage('Release transport'){
    node {
        step([$class: 'StepChangeReleaseDevelopmentTransport', ChangeID: params.CHANGE_ID])
    }
}
