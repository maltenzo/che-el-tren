# Ficha Google Play — Che, el tren

**Nombre de la app:** Che, el tren

**Descripción corta** (máx 80 car.):
Próximos trenes en vivo en tu reloj. Arribos de Trenes Argentinos, al instante.

**Descripción completa:**
Che, el tren te dice cuántos minutos faltan para el próximo tren, directo en tu
reloj Wear OS.

• Arribos en tiempo real de cualquier estación de la red (SOFSE).
• Elegí ramal y estación desde el reloj; guardá tus favoritos (mantené apretado).
• Próximo tren y los siguientes, con swipe.
• Se actualiza solo cada minuto.
• Simple, rápido y sin vueltas.

Ideal para cuando estás por salir y querés saber si llegás al andén.

App no oficial. No tiene relación con SOFSE / Trenes Argentinos. Usa datos
públicos de arribos. "Trenes Argentinos" y su logo pertenecen a sus titulares.

**Categoría:** Mapas y navegación (o Viajes y guías locales)
**Tipo:** Gratis
**Contiene anuncios:** No
**Público:** Todos

---

## Assets necesarios para subir

- [x] Icono 512×512 → `playstore_icon_512.png`
- [ ] Gráfico destacado 1024×500 (banner) — pendiente
- [ ] Capturas Wear OS (mín 1, cuadradas/redondas 384×384 o 450×450) — sacar del reloj
- [x] AAB firmado → `app/build/outputs/bundle/release/app-release.aab`
- [ ] Política de privacidad (URL) — requerida por Play. Como no recolectás datos
      personales, alcanza con una página simple diciendo eso (ver borrador abajo).

## Pasos para publicar

1. Crear cuenta de desarrollador en Google Play Console (US$25, una vez).
2. Crear app → Wear OS → subir el AAB en un track (interno/cerrado primero).
3. Completar ficha con los textos de arriba + assets.
4. Data safety: declarar "no se recolectan datos".
5. Política de privacidad: subir la URL.
6. Enviar a revisión.

## Borrador de política de privacidad (hosteala en cualquier lado)

> Che, el tren no recolecta, almacena ni comparte datos personales. La app
> consulta una API pública de arribos de trenes y guarda tus preferencias
> (estación y favoritos) únicamente en tu dispositivo. No usa analítica ni
> publicidad. Permiso de ubicación: no requerido.
> Contacto: evigneti@gmail.com
