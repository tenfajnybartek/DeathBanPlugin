# DeathBanPlugin

Plugin na bana po śmierci dla PaperMC 1.21+. Gracz po śmierci zostaje zbanowany na czas określony w configu.


## Konfiguracja

mysql:
  host: "localhost"
  port: 3306
  user: "deathban"
  password: "supersecret"
  database: "deathban"

settings:
  deathban_seconds: 86400 # ile sekund trwa ban po śmierci (domyślnie 24h)
  exempt_permission: "deathban.vip"
```

## Komendy

- `/deathban unban <nick>` – odbanuje wybranego gracza
- `/deathban unbanall` – odbanuje wszystkich
- `/deathban checkban <nick>` – sprawdza czy gracz jest zbanowany
- `/deathban reload` – przeładowuje konfigurację

## Uprawnienia

- `deathban.command.use` – pozwala korzystać z komendy `/deathban*`
- `deathban.vip` – zwalnia z bana po śmierci

## Wymagania

- Serwer PaperMC 1.21+
- Baza MySQL

## Autor

`tenfajnybartek`
