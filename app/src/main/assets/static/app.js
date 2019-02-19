(function () {

  var App = function App() {

  };

  App.prototype.init =
    function () {
      this.bindViews();
      this.initViews();
    }

  App.prototype.bindViews = function () {
    this.upload = document.getElementById('formFileInputCt');
    this.uploadFile = document.getElementById('uploadFile');
  };
  App.prototype.initViews = function () {
    if (this.upload) {
      var that = this;
      this.upload.addEventListener('click', function () {
        that.toggleUploadMenu();
      });
    }
    if (this.uploadFile) {
      this.uploadFile.addEventListener('click', function () {
        window.Upload.uploadFile();
      })
    }
  };
  App.prototype.toggleUploadMenu =
    function () {
      if (this.upload.classList.contains('act')) {
        this.upload.classList.remove('act');
      } else {
        this.upload.classList.add('act');
      }
    }

  var app = new App();
  app.init();
})();