from django.db import models


class SecFile(models.Model):
    file_name = models.CharField(max_length=100)
    sec_file = models.FileField(upload_to="sec_file/")