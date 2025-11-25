# 🚨 SOS Mujer - Aplicación de ayuda para emergencias

**SOS Mujer** es una aplicación móvil desarrollada en Android cuyo principal objetivo es brindar una herramienta rápida y accesible para mujeres que se encuentren en una situación de peligro. Esta app permite reportar incidentes, compartir la ubicación en tiempo real y contactar a personas de confianza con un solo botón.

---

## 🧭 ¿Qué hace esta app?

- **Envía alertas a contactos de emergencia:**  
  Cuando una persona presiona el botón de pánico, la aplicación prepara un mensaje con una alerta de emergencia que incluye su ubicación actual y lo envía por SMS a sus contactos registrados (máximo 3).

- **Registra reportes de abuso o incidentes:**  
  La usuaria puede reportar un hecho incluyendo una foto como evidencia, el tipo de abuso, una descripción y su ubicación, todo en un solo formulario fácil de usar.

- **Ubica zonas con más reportes en un mapa:**  
  Muestra puntos críticos en un mapa, agrupando zonas donde hay múltiples reportes y destacando los incidentes más recientes con íconos personalizados.

---

## 📱 ¿Cómo funciona?

- La usuaria registra hasta 3 contactos de confianza.
- En una situación de peligro, puede presionar un botón de pánico que lanza automáticamente un mensaje de ayuda.
- También puede llenar un reporte con foto, ubicación y detalles del incidente.
- Desde la app puede ver un mapa interactivo con las zonas más reportadas.

---

## 🧪 ¿Qué se usó para desarrollarla?

- **Java** y **Android Studio** para la aplicación móvil
- **Google Maps API** para el mapa de reportes
- **MySQL** y **PHP** para el backend (servidor)
- **SQLite** para guardar los datos de sesión localmente
- **SMSManager** para enviar mensajes de texto
- **FusedLocationProviderClient** para obtener la ubicación

---

## 💡 Detalles importantes

- La app funciona sin necesidad de Internet para enviar mensajes SMS (ideal en emergencias).
- Los reportes se suben al servidor y quedan registrados con hora, tipo de incidente y ubicación.
- El envío automático de SMS está limitado en Android 10 o superior, por lo que se abre la app de mensajes con el mensaje listo (por seguridad de Google).
- Se recomienda usar Android 9 o anterior si se desea enviar SMS sin interacción del usuario.

---

## 🧭 ¿Qué se necesita para correrla?

- Un dispositivo Android (idealmente versión 8 o 9)
- Conexión a Internet para registrar contactos y enviar reportes
- Activar permisos: ubicación, cámara, almacenamiento y SMS
- Tener la ubicación activada para obtener la dirección al reportar

---

## 📌 Recomendaciones

- Probar la app en un dispositivo físico para verificar el envío de SMS.
- Registrar al menos un contacto de emergencia antes de usar el botón de pánico.
- Tener habilitada la app de mensajes como predeterminada para garantizar la funcionalidad de envío.

---

## 🙋‍♀️ ¿Quién hizo esta app?

Este proyecto fue desarrollado como parte de una iniciativa para brindar herramientas tecnológicas de apoyo a mujeres en riesgo, dentro del contexto académico de la Universidad Privada del Norte (UPN).

**Desarrollado por:** Grupo +51Code

---

## 🔒 Licencia

Este proyecto es libre de usar, estudiar y modificar con fines educativos. Si lo vas a compartir o reutilizar, ¡recuerda dar créditos!

