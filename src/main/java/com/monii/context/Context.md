# Context Java backend

Proyectos: Monii (https://www.notion.so/Monii-1e6a26838e858030932af15b2011edb7?pvs=21)

## Problema a resolver:

App web para registrar finanzas personales con foco en préstamos/deudas. Se registran ingresos, egresos, deudas que doy, préstamos que pido, pagos programados. Todo enlazado con contactos, que pueden ser personas, plataformas o entidades, y así mismo, enlazados con categorías para tener una visión completa del uso del dinero.

La aplicación busca ayudar a los usuarios a registrar y administrar sus finanzas personales, con especial énfasis en el seguimiento de préstamos y deudas. Ofrece una visión completa del uso del dinero al vincular las transacciones con contactos y categorías.

## Cómo Funciona:

La aplicación permite a los usuarios registrar diversos movimientos financieros, incluyendo ingresos, gastos, deudas concedidas, préstamos solicitados y pagos programados. Estos movimientos se vinculan a contactos (personas, plataformas o entidades) y categorías. La aplicación ofrece diferentes vistas para gestionar estos movimientos, incluyendo un resumen del panel, vistas detalladas de movimientos, deudas, préstamos, pagos programados, contactos y categorías. El panel ofrece un resumen de la actividad financiera con tarjetas, gráficos y tablas.

## Objetivos de la Experiencia del Usuario:

- Proporcionar una interfaz clara e intuitiva para el seguimiento de las finanzas personales.
- Ofrecer un enfoque específico para la gestión de préstamos y deudas.
- Facilitar un seguimiento completo al vincular las transacciones con contactos y categorías.
- Presentar los datos financieros en resúmenes fáciles de entender y vistas detalladas.
- Facilitar acciones rápidas, como añadir nuevos movimientos desde el panel. - Garantice un flujo de autenticación seguro con opciones de 2FA y recuperación de contraseña.

# Pilares de la funcionalidad

## Movimientos

### Registro

- Tipo (Ingreso, Egreso, Deuda, Préstamo, Pago Programado)
- Monto, Descripción, Fecha
- Contacto enlazado/involucrado
- Estado
    
    El estado lo marca automáticamente la lógica de negocio con las siguientes reglas
            - Si `saldo restante === 0` → `Pagado`
            - Si `fecha de vencimiento < hoy` y `saldo restante > 0` → `Vencido`
            - Si `fecha de vencimiento >= hoy` y `saldo restante > 0` → `Pendiente`
    
- Categoría enlazada

### Api actual para registro de movimientos

```markdown
# Api registro de movimientos

## Post

- Income & Expense
    - Request
        
        ```json
        {
            "type": "INCOME",
            "amount": 1500.00,
            "description": "Salario mensual1"
        }
        {
            "type": "EXPENSE",
            "amount": 200.00,
            "description": "Supermercado"
        }
        ```
        
    - Response
        
        ```json
        {
            "id": 12,
            "type": "INCOME",
            "amount": 1500.00,
            "description": "Salario mensual1",
            "createdAt": "2025-04-24T16:47:14.2356033"
        }
        {
            "id": 13,
            "type": "EXPENSE",
            "amount": 200.00,
            "description": "Supermercado",
            "createdAt": "2025-04-24T16:47:21.3336124"
        }
        ```
        
- Loan
    - Request
        
        ```json
        {
            "type": "LOAN",
            "amount": 500.00,
            "description": "Préstamo a Juan",
            "counterpartyId": 1,
            "dueDate": "2025-12-31T23:59:59"
        }
        ```
        
    - Response
        
        ```json
        {
            "id": 14,
            "type": "LOAN",
            "amount": 500.00,
            "description": "Préstamo a Juan",
            "createdAt": "2025-04-24T16:47:30.5733918",
            "counterparty": {
                "counterpartyId": 1,
                "counterpartyName": "BBVA",
                "counterpartyType": "COMPANY",
                "totalAmount": null
            },
            "dueDate": "2025-12-31T23:59:59",
            "isPaid": false
        }
        ```
        
- Debt
    - Request
        
        ```json
        {
            "type": "DEBT",
            "amount": 300.00,
            "description": "Préstamo personal con Banco XYZ",
            "counterpartyId": 1,
            "dueDate": "2025-12-31T23:59:59"
        }
        ```
        
    - Response
        
        ```json
        {
            "id": 15,
            "type": "DEBT",
            "amount": 300.00,
            "description": "Préstamo personal con Banco XYZ",
            "createdAt": "2025-04-24T16:47:36.5719198",
            "counterparty": {
                "counterpartyId": 1,
                "counterpartyName": "BBVA",
                "counterpartyType": "COMPANY",
                "totalAmount": null
            },
            "dueDate": "2025-12-31T23:59:59",
            "isPaid": false
        }
        ```
        
- Recurrent (pagos programados)
    - Request
        
        ```json
        {
            "type": "RECURRENT",
            "amount": 19.99,
            "description": "Spotify Premium",
            "frequency": "MONTHLY",
            "counterpartyId": 1,
            "startDate": "2025-04-25T00:00:00",
            "endDate": "2026-12-31T23:59:59"
        }
        ```
        
    - Response
        
        ```json
        {
            "id": 17,
            "type": "RECURRENT",
            "amount": 19.99,
            "description": "Spotify Premium",
            "createdAt": "2025-04-24T16:48:01.6211661",
            "frequency": "MONTHLY",
            "startDate": "2025-04-25T00:00:00",
            "endDate": "2026-12-31T23:59:59",
            "isActive": true,
            "counterparty": {
                "counterpartyId": 1,
                "counterpartyName": "BBVA",
                "counterpartyType": "COMPANY",
                "totalAmount": null
            }
        }
        ```
```

### TODO Movimientos:

1. Agregar la lógica de negocio para el estado de cada movimiento siguiendo las reglas anteriormente mencionadas:
    - Si `saldo restante === 0` → `Pagado`
    - Si `fecha de vencimiento < hoy` y `saldo restante > 0` → `Vencido`
    - Si `fecha de vencimiento >= hoy` y `saldo restante > 0` → `Pendiente`
2. Agregar campo para enlazar la categoría (aplica únicamente para Pagos programados y Préstamos
3. Endpoint para Editar cada uno de los movimientos
4. Endpoint para Eliminar cada uno de los movimientos
5. Endpoint para filtrar los movimientos por:
    - Tipo (Ingreso, Egreso, Deuda, Préstamo, Pago programado)
    - Rango de fechas
    - Contacto enlazado
    - Estado
    - Categoría
6. Endpoint para listar todos los movimientos

## Contactos

Registrar y ver vinculación de personas o entidades con los movimientos.

Aún no tengo ningún endpoint para contactos, por lo cual se debe crear:

### Endpoint de registro

- Nombre
- Tipo (Persona, Entidad, Plataforma)
- Card Id
- Correo
- Celular

### Endpoints para obtener/listar contactos

1. Listar datos básicos (mismos que campos que el registro)
2. Listar contacto con:
    - Nombre
    - Correo
    - Celular
    - Total deuda pendiente
    - Total préstamo pendiente
3. Listar movimientos asociados al contacto
    - Fecha de registro y/o fecha de vencimiento
    - Estado
    - Descripción
    - Monto
        - Deudas y préstamos
            - Monto total
            - Monto pendiente
        - Pagos programados
            - Monto a pagar
            - frecuencia (diario, semanal, mensual, anual)

### Endpoint para editar datos básicos de contacto

### Endpoint para eliminar contacto

## Categorías

Etiquetar los movimientos para categorizar el flujo del dinero

Aún no tengo ningún endpoint para contactos, por lo cual se debe crear:

### Endpoint de registro

- Nombre (nombre único, no se puede repetir)
- Color (establecer una lista de colores pastel por defecto)

### Endpoint para editar

Mismos campos que el registro

### Endpoint para eliminar

### Endpoint para listar

- Nombre
- Color