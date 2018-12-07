
# GokuRakuJoudo

![img](https://travis-ci.com/yqrashawn/GokuRakuJoudo.svg?branch=master)
![img](https://coveralls.io/repos/github/yqrashawn/GokuRakuJoudo/badge.svg)

[Examples](./Examples.org) &mdash;
[Tutorial](./Tutorial.md) &mdash;
[Telegram](https://t.me/karabinermac) &mdash;
[Goku in the wild](./In-The-Wild.md)

1.  [Intro](#intro)
    1.  [Why use Goku?](#why)
    2.  [Install](#install)
    3.  [Usage](#usage)
    4.  [Tutorial](#tutorial)
    5.  [Note](#note)
    6.  [Changelog](#changelog)
    7.  [Dependencies](#dependencies)
    8.  [Contributing](#contributing)



<a id="GokuRakuJoudo"></a>

# Intro

Goku is a tool to let you manage your [Karabiner](https://github.com/tekezo/Karabiner-Elements) configuration with ease.

Karbiner Elements uses JSON as it's config file. This leads to thousands lines of JSON (sometimes over 20,000 lines). Which makes it really hard to edit the config file and iterate on your keymap.

Goku use the [edn format](https://github.com/edn-format/edn) to the rescue.


<a id="why"></a>

## Why use Goku?

Below are two Karabiner configuration snippets that map caps lock to an escape key. 

<div class="HTML">
<p align="center"><img src="resources/images/karabiner.json.png" /></p>
<p align="center">karabiner.json</span>
</div>

<div class="HTML">
<p align="center"><img src="resources/images/karabiner.edn.png" /></p>
<p align="center">karabiner.edn</span>
</div>

In practice this means that you can see multiple Karabiner rules on you screen as you edit your config with Goku. Which speeds up the iteration speed significantly as you can create new rules as little as few characters. 


<a id="install"></a>

## Install

    brew install yqrashawn/goku/goku


<a id="usage"></a>

## Usage

Create a profile named "Goku" in Karabiner GUI tool.

![img](./resources/images/karabiner-profile.png)

Goku reads `karabiner.edn` file which holds your Karabiner config. This file should be placed inside \`~/.config/\` directory on your mac. If you use a dots dir to hold your config files, you can symlink \`karabiner.edn\` and Goku will pick up the changes too.

Goku provides two commands:

`goku`: Will update karabiner.json once.
`gokuw`: Will keep watching your `karabiner.edn` and on saving, will update your
configuration. 

Run command `brew services start goku` to use it as a service (runs `gokuw` in background). When Goku is ran as service, the logs are kept inside `~/Library/Logs/goku.log`. 


<a id="tutorial"></a>

## Tutorial

Read through the [Tutorial about how you can write the configuration in Goku](./Tutorial.md).

If there's any question or advice, just [open an issue](../../issues/new) or join [Karabiner Telegram group](https://t.me/karabinermac) and ask your questions there.

<a id="note"></a>

## Note

-   Using `#_` to comment out rules [like this](https://github.com/yqrashawn/yqdotfiles/blob/2699f833f9431ca197d50f6905c825712f7aee8d/.config/karabiner.edn#L41) will leave a null rule (see below) in karabiner.json, it won't cause any error.

```json
    {
      "description" : null,
      "manipulators" : [ ]
    }
```

<a id="changelog"></a>

## Changelog

Check [CHANGELOG](./CHANGELOG.org) file.

<a id="dependencies"></a>

## Dependencies

[watchexec](https://github.com/watchexec/watchexec) for watching edn config file.   
[joker](https://github.com/candid82/joker) for linting edn config file.   

<a id="contributing"></a>

## Contibuting

Use `lein repl` for developing.

Change [Makefile.local.example](./Makefile.local.example)'s name to `Makefile.local`, change `GRAALVM` variable in the makefile to right path, run `make local` to test then generate `goku` binary file. 