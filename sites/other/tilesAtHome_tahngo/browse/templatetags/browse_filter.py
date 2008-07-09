from django import template
register = template.Library()

@register.filter
def div(value, arg):
   return value / arg
