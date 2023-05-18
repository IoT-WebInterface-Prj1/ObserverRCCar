"""
URL configuration for config project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/4.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path, include
<<<<<<< HEAD
=======
from django.conf.urls.static import static
from django.conf import settings
>>>>>>> 733a5a0d781f3ce9958712d274184754d15f420f

urlpatterns = [
    path('admin/', admin.site.urls),
    path('mjpeg/', include('mjpeg.urls')),
<<<<<<< HEAD
]
=======
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
>>>>>>> 733a5a0d781f3ce9958712d274184754d15f420f
