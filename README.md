# biglie — StreamingCommunity per CloudStream

Fork di [doGiorsHadEnough](https://github.com/doGior/doGiorsHadEnough) dedicato a
**StreamingCommunity** per CloudStream, con dominio aggiornato automaticamente dal
canale Telegram ufficiale [@Streaming_community_sito](https://t.me/Streaming_community_sito).

## Cosa cambia rispetto all'originale

- Dominio predefinito aggiornato a `streamingcommunityz.nexus`.
- Il plugin prova a leggere l'ultimo dominio direttamente dalla pagina pubblica del
  canale Telegram (`https://t.me/s/Streaming_community_sito`) all'avvio.
- Se il dominio cambia, una GitHub Action aggiorna il codice, incrementa la versione
  del plugin e pubblica una nuova build: CloudStream mostra l'aggiornamento.
- Estrazione video tramite **VixSrc** (API `/api/movie/...` e `/api/tv/...`), che
  attualmente è la sorgente funzionante.
- Solo il plugin StreamingCommunity, per build più veloci e affidabili.

## Installazione in CloudStream (Android)

1. Apri CloudStream.
2. Vai in **Impostazioni → Estensioni → Aggiungi repository**.
3. Nel campo **Nome** scrivi `biglie`.
4. Nel campo **URL** incolla:

   ```text
   https://raw.githubusercontent.com/fra06083/biglie/builds/repo.json
   ```

5. Salva, apri il repository e installa **StreamingCommunity**.

L'URL del repository è stabile: la branch `builds` viene aggiornata a ogni build.

> **Importante:** l'URL da inserire in CloudStream è quello di `repo.json`
> (il descrittore del repository), **non** `plugins.json` (la lista dei plugin).
> Se CloudStream mostra un errore di parsing, controlla di aver usato l'URL giusto.

### Se CloudStream non aggiunge il repository

- Verifica che l'URL finisca con `repo.json`.
- Elimina il repository vecchio e aggiungilo di nuovo.
- Se l'errore compare subito dopo un aggiornamento, GitHub può servire la
  versione precedente per qualche minuto. Usa questo URL alternativo:
  `https://github.com/fra06083/biglie/raw/refs/heads/builds/repo.json`.
- Se `raw.githubusercontent.com` non è raggiungibile dalla tua rete, usa
  l'URL alternativo qui sopra (passa da `github.com`).

### Link diretto per Android

Dal telefono puoi aprire direttamente questo link: CloudStream si apre e carica il
repository senza dover copiare l'URL.

```text
cloudstreamrepo://raw.githubusercontent.com/fra06083/biglie/builds/repo.json
```

Se il link non è cliccabile, incollalo nella barra degli indirizzi del browser oppure
usa il pulsante **Aggiungi repository** e incolla l'URL `https://...` qui sopra.

### Vuoi usare solo la parola `biglie`?

CloudStream, se nel campo URL scrivi una sola parola, la risolve tramite
`cutt.ly` (oppure `py.md` se scrivi `!parola`). Per esempio:

- crea un link gratuito su [cutt.ly](https://cutt.ly) con alias `biglie` che punti a
  `https://raw.githubusercontent.com/fra06083/biglie/builds/repo.json`;
- poi in CloudStream scrivi semplicemente `biglie` nel campo URL.

In alternativa registrati su [py.md](https://py.md), crea il link `py.md/biglie` e in
CloudStream scrivi `!biglie`.

Senza alias, usa l'URL completo indicato sopra (funziona subito).

## Registrare il repository nella lista pubblica

CloudStream non ha un registro centrale: ogni utente aggiunge i repository che vuole.
Per rendere il progetto visibile alla community puoi aggiungere una riga con nome e URL
del `repo.json` alla pagina wiki [List of extensions](https://cloudstream.miraheze.org/wiki/List_of_extensions)
(serve un account Miraheze) oppure alla lista [cs3-repos](https://rentry.org/cs3-repos).
L'installazione nell'app resta quella descritta sopra.

## Aggiornamento automatico del dominio

- `scripts/update_domain.py` legge l'ultimo messaggio con `Nuovo:` dal canale Telegram,
  verifica che il dominio risponda e aggiorna provider, icona e versione.
- `.github/workflows/update-domain.yml` lo esegue ogni 6 ore.
- La modifica fa partire `.github/workflows/build.yml`, che pubblica il nuovo `.cs3`,
  il nuovo `plugins.json` e il descrittore `repo.json` nella branch `builds`.

Per aggiornare manualmente:

```bash
python3 scripts/update_domain.py
git add -A
git commit -m "chore: update domain"
git push
```

## Build locale

```bash
export ANDROID_HOME="$HOME/Android/Sdk"   # oppure il tuo SDK
./gradlew make makePluginsJson
```

Il plugin compilato si trova in `StreamingCommunity/build/`.
