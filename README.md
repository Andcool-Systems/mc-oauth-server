# Minecraft OAuth server

## Preface

Implementation of the Minecraft authentication flow in Java. After developing the [mc-oauth plugin](https://github.com/Andcool-Systems/mc-oauth), I was very disappointed with the load on my VPS from the original Minecraft server, and even the PaperMC core did not significantly improve the situation. So, I decided to write a server from scratch that would only include the authentication system, without burdening the server with the game world.

All endpoints and response types remain the same as in the plugin.

>[!Note]
> This server does not contain game state implementation and cannot be used for full-fledged gameplay.

## Detailed description

The main stages of establishing a connection with the server:  
C–Client S–Server
1. **C -> S** Handshake
2. **C -> S** Login start
3. **S -> C** Encryption request
4. **Client auth**
5. **C -> S** Encryption response
6. **Server auth + generate code**
7. **S -> C** Code response

The server does not have an offline mode setting, as it would not make sense in this case, so it always tries to authenticate the player through Mojang.  
Currently, the server has no settings and all parameters are fixed.

TODO:
- [x] Add server icon support. 
- [ ] Add the ability to choose the authentication server.
- [ ] Add configuration.
- [ ] Add a proper logger (instead of the custom SillyLogger).
- [x] Add text formatter for MOTD.
