# Coffee App

App Android para baristas e entusiastas registrarem receitas e preparos de café.
Funciona 100% offline: todos os dados ficam só no celular, sem conta.

## O que o app faz

- **Receitas**: cadastre receitas com antecedência (método, café, moedor, ajuste de moagem,
  dose, despejos com tempo e gramas, temperatura, tempo-alvo e notas). Já vem com 3 receitas-modelo
  (V60, Melitta e prensa francesa) para quem está começando.
- **Preparo guiado**: abra uma receita e toque em *Preparar*. O timer mostra quando fazer cada
  despejo, quantos gramas despejar e até quanto a balança deve chegar (vibra a cada novo despejo).
- **Preparo livre**: toque em *Iniciar preparo* junto com o primeiro despejo. A cada novo despejo,
  toque em *Novo despejo*. No final, toque em *Finalizar*, preencha as gramas de cada despejo e salve.
  Dá para salvar também como receita.
- **Histórico**: todos os preparos salvos, com proporção, tempo, moagem, café e nota (estrelas).
  De um preparo dá para criar uma receita ou repetir a receita usada.

Métodos do MVP: V60, Melitta e prensa francesa. Campos menos usados (temperatura, tempo, notas)
ficam em **Opções avançadas**, para manter a tela simples para iniciantes.

## Como rodar

### Opção 1: baixar o APK pronto (sem instalar nada)
1. Abra a aba **Actions** do repositório no GitHub e entre na execução mais recente do
   *Android CI* (verde).
2. Em **Artifacts**, baixe `coffee-app-debug-apk` e descompacte.
3. Envie o `app-debug.apk` para o celular Android (8.0 ou mais novo) e abra-o. O Android vai pedir
   para permitir a instalação de "fontes desconhecidas".

### Opção 2: Android Studio
1. Instale o [Android Studio](https://developer.android.com/studio) (Ladybug ou mais novo).
2. *File → Open* e escolha a pasta do repositório. Aguarde o Gradle sincronizar.
3. Conecte o celular com a depuração USB ligada (ou crie um emulador) e clique em **Run ▶**.

### Opção 3: linha de comando
Requer JDK 17 e o Android SDK (variável `ANDROID_HOME` configurada).

```bash
./gradlew testDebugUnitTest   # testes
./gradlew assembleDebug       # gera app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug        # instala no celular conectado
```

## Tecnologia

- Kotlin + Jetpack Compose (Material 3)
- Room (SQLite local) para receitas e preparos
- Navigation Compose, ViewModel + StateFlow
- minSdk 26 (Android 8.0), targetSdk 35

## Estrutura

```
app/src/main/java/com/heitoranglada/coffeeapp/
├── domain/   # regras puras: métodos, despejos, formatação, passos do preparo guiado
├── data/     # Room: entidades, DAOs, banco e repositório (+ receitas-modelo)
└── ui/
    ├── recipes/   # lista, detalhe e cadastro de receitas
    ├── brew/      # timer: preparo guiado e preparo livre
    ├── history/   # histórico de preparos
    └── components/# formulário e componentes compartilhados
```
