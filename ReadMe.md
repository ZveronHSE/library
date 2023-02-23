# Платформенная утилита 
## Logging GRPC
В данный момент поддержаны логгирование для client и server, они включены по умолчанию. 

Чтобы настраивать гибкое выключение или включение логгирований, нужно в application.yml добавлять:
```yaml
platform:
  grpc:
    client:
      logging: true
    server:
      logging: true
```

Также вы можете переопределить метод logMessage для настройки логгирования по своему. Для клиентов нужно только 
наследовать от LoggingClientInterceptor, для серверов LoggingServerInterceptor

## ApiGateway 
Если у вас сервис имеет внешние эндпоинты, можно будет заиспользовать интерцептор MetadataApiGatewayInterceptor, 
который отвечает за то, чтобы получить все метаданные от ApiGateway и пользоваться этим. По умолчанию выключено.

Чтобы включить: 
```yaml
platform:
  grpc:
    apigateway:
      metadata: true
```