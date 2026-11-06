# biglie — StreamingCommunity per CloudStream

Fork di [doGiorsHadEnough](https://github.com/doGior/doGiorsHadEnough) dedicato a
**StreamingCommunity** per CloudStream, con dominio aggiornato automaticamente dal
canale Telegram ufficiale [@Streaming_community_sito](https://t.me/Streaming_community_sito).

## Cosa cambia rispetto all'originale

- Dominio predefinito aggiornato a `streamingcommunityz.boo`.
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
   https://raw.githubusercontent.com/fra06083/biglie/builds/plugins.json
   ```

5. Salva, apri il repository e installa **StreamingCommunity**.

L'URL del repository è stabile: la branch `builds` viene aggiornata a ogni build.

### Vuoi usare solo la parola `biglie`?

CloudStream, se nel campo URL scrivi una sola parola, la risolve tramite
`cutt.ly` (oppure `py.md` se scrivi `!parola`). Per esempio:

- crea un link gratuito su [cutt.ly](https://cutt.ly) con alias `biglie` che punti a
  `https://raw.githubusercontent.com/fra06083/biglie/builds/plugins.json`;
- poi in CloudStream scrivi semplicemente `biglie` nel campo URL.

In alternativa registrati su [py.md](https://py.md), crea il link `py.md/biglie` e in
CloudStream scrivi `!biglie`.

Senza alias, usa l'URL completo indicato sopra (funziona subito).

## Registrare il repository nella lista pubblica

CloudStream non ha un registro centrale: ogni utente aggiunge i repository che vuole.
Per rendere il progetto visibile alla community puoi aggiungere una riga con nome e URL
del `plugins.json` alla pagina wiki [List of extensions](https://cloudstream.miraheze.org/wiki/List_of_extensions)
(serve un account Miraheze) oppure alla lista [cs3-repos](https://rentry.org/cs3-repos).
L'installazione nell'app resta quella descritta sopra.

## Aggiornamento automatico del dominio

- `scripts/update_domain.py` legge l'ultimo messaggio con `Nuovo:` dal canale Telegram,
  verifica che il dominio risponda e aggiorna provider, icona e versione.
- `.github/workflows/update-domain.yml` lo esegue ogni 6 ore.
- La modifica fa partire `.github/workflows/build.yml`, che pubblica il nuovo `.cs3`
  e il nuovo `plugins.json` nella branch `builds`.

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
