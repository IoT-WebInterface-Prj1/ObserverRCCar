from django.db import models

<<<<<<< HEAD
# Create your models here.
=======
class SecFile(models.Model):
    file_name = models.CharField(max_length=100)
    sec_file = models.FileField(upload_to="sec_file/%Y/%m/%d/")

>>>>>>> 733a5a0d781f3ce9958712d274184754d15f420f
