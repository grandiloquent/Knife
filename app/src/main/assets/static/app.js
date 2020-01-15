;(function () {

    class Upload {

        constructor() {
            this.uploadButton = document.getElementById('uploadButton');
        }

        bindEvents() {
            if (this.uploadButton) {
                this.uploadButton.addEventListener('click', this.onUploadButtonClick.bind(this));
            }
        }


        onInputFileChange(event) {
            const files = event.target.files;
            const formData = new FormData();
            for (let i = 0; i < files.length; i++) {
                formData.append("file", files[i], files[i].name);
            }
            this.onUpload(formData);
        }

        onUpload(formData) {
            fetch("/upload", {
                method: 'post',
                body: formData
            }).then(function (response) {
                return response.json();
            }).then(function (obj) {

            }).catch((error) => {
                console.log(error);
            })
        }

        onUploadButtonClick() {
            if (!this.input) {
                const input = document.createElement('input');
                input.setAttribute('type', 'file');
                input.setAttribute('multiple', '');
                document.body.appendChild(input);
                input.oninput = this.onInputFileChange.bind(this);
                this.input = input;
            }
            this.input.click();
        }

    }

    const upload = new Upload();
    upload.bindEvents();

})();