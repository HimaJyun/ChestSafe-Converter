# ChestSafe-Converter
This plugin is for converting LWC and ChestSafe databases for ChestSafe.  
[日本語](https://github.com/HimaJyun/ChestSafe-Converter/blob/master/README_ja.md)

Be sure to **back up** your database before using this plugin.  
(LWC、ChestSafe(SQLite)、ChestSafe(MySQL)……)

## Tested combination
Operation has been confirmed by the following combination.

- Spigot 1.13.2
- ChestSafe 1.1.0
- ModernLWC 2.1.2

(The implementation works with the original LWC, but the original LWC no longer works with Spigot 1.13.2)

# Convert
The work uses `/chestsafe-convert` command.

If you want to reset during configuration you can start over with `/chestsafe-convert reset`.

**Be sure to get a backup before working.**

I warned.

## LWC -> ChestSafe
Please set ChestSafe in advance.

At the time of execution, `LWC`, `ChestSafe`, and `ChestSafe-Converter` are all required.

1. Execute `/chestsafe-convert lwc` command.
1. Execute `/chestsafe-convert [speed]` command.  
   Speed ​​specified by number, recommended to be slow (about 100)
1. Execute `/chestsafe-convert confirm` command.  
   Let's wait for completion.

`Donation` and `Password` is not supported.  
If these protections are encountered, they will be converted to private chests.

It is ignored if it encounters a flag not supported by ChestSafe.

## ChestSafe(SQLite) -> ChestSafe(MySQL)
To convert from SQLite to MySQL, configure ChestSafe to use MySQL.

At the time of execution, `ChestSafe`, and `ChestSafe-Converter` are all required.

1. Execute `/chestsafe-convert chestsafe` command.
1. Execute `/chestsafe-convert sqlite` command.
1. Execute `/chestsafe-convert [sqlite path]` command.  
   Get from setting if not specified.
1. Execute `/chestsafe-convert [speed]` command.  
   Speed ​​specified by number, recommended to be slow (about 100)
1. Execute `/chestsafe-convert confirm` command.  
   Let's wait for completion.

## ChestSafe(MySQL) -> ChestSafe(SQLite)
To convert from MySQL to SQLite, configure ChestSafe to use SQLite.  

At the time of execution, `ChestSafe`, and `ChestSafe-Converter` are all required.

1. Execute `/chestsafe-convert chestsafe` command.
1. Execute `/chestsafe-convert mysql` command.
1. Execute `/chestsafe-convert [host]` command.
   Get from setting if not specified.
1. Execute `/chestsafe-convert [database]` command.  
   Get from setting if not specified.
1. Execute `/chestsafe-convert [username]` command.  
   Get from setting if not specified.
1. Execute `/chestsafe-convert [password]` command.  
   Get from setting if not specified.
1. Execute `/chestsafe-convert [speed]` command.  
   Speed ​​specified by number, recommended to be slow (about 100)
1. Execute `/chestsafe-convert confirm` command.  
   Let's wait for completion.
