# Crews (Paper 26.3)

Plugin de crews/grupos/hermandades con menu GUI con textura propia y el nombre
de la crew debajo del nametag.

## Obtener el .jar (sin compilar en tu PC)
1. Crea un repo en GitHub y sube todo el contenido de esta carpeta (rama `main`).
2. Pestana **Actions** -> workflow **Build** -> abre la ejecucion -> **Artifacts** -> `Crews`
   (contiene `Crews-1.0.0.jar`, `Crews-ResourcePack.zip` y `Crews-ResourcePack.sha1`).
3. Para un Release fijo: `git tag v1.0.0 && git push origin v1.0.0` y los archivos
   quedan en **Releases**.
4. Pon el `.jar` en `plugins/`. Requiere **Java 25** y **Paper 26.3**.

## Carpeta `build` en el repo
Cada vez que subas cambios a `main`, el workflow compila y guarda en el propio repo la
carpeta **`build/libs/`** con `Crews-1.0.0.jar`, `Crews-ResourcePack.zip` y
`Crews-ResourcePack.sha1`. Para descargar el jar entra a `build/libs/` en GitHub y pulsa
el archivo -> **Download raw file**. (En Settings -> Actions -> General -> Workflow
permissions debe estar **Read and write permissions**.)

## Resource pack (textura del menu)
Puedes usar el zip que queda en `build/libs/` (URL raw) o el del Release. Copia en `plugins/Crews/config.yml`:
```yaml
resource-pack:
  url: "https://github.com/TU_USUARIO/TU_REPO/releases/download/v1.0.0/Crews-ResourcePack.zip"
  sha1: "<contenido de Crews-ResourcePack.sha1>"
```
Sin el pack el menu funciona igual, pero sin el fondo personalizado.

## Uso
- Abrir menu: **Shift + F** o `/crew`
- Crear: boton del menu (nombre -> color -> estandarte) o `/crew create <nombre> [color]`
- Color del nombre bajo el tag: menu (16 colores) o `/crew color <color>`
- Estandarte: en el menu puedes **disenarlo** (color base + hasta 6 capas con los patrones
  vanilla) o usar el que llevas en la mano (`/crew banner` con el estandarte en la mano
  principal). Solo el lider puede cambiarlo.
- Invitar: boton del menu o `/crew invite <jugador>` (el jugador debe estar online)
- `/crew accept|deny [crew]`, `/crew leave`, `/crew kick <jugador>`,
  `/crew rename <nombre>`, `/crew disband`, `/crew info`

## Limitaciones
- La tecla **H** no se puede detectar desde un plugin de servidor; solo llegan al
  servidor teclas como F o Shift. Para H hace falta un mod de cliente.
- El nombre bajo el nametag es un TextDisplay: ajusta `nametag.y-offset` en la config.
