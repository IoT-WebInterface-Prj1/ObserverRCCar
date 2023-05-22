from django.shortcuts import render
from django.views.generic import TemplateView
from django.urls import path

# Create your views here.
class StateView(TemplateView):
    template_name = "state.html" # 랜더링할 템플릿 파일 경로
    