## New modifiers symbols vs Current
  |↓Label type    	| Key→	|Shift	|Ctrl	|Ctrl	|Alt 	|Alt 	|Cmd 	|Cmd 	|Cmd 	|
  |:--------------	| :-  	|  :-:	| :-:	| :-:	| :-:	| :-:	| :-:	| :-:	| :-:	|
  |Any (mandatory)	|     	| ⇧   	|  ⎈ 	| ⌃  	|  ⎇ 	| ⌥  	| ⌘  	| ◆  	| ❖  	|
  |+ Left         	|‹ `'`	| ‹⇧  	|    	|    	|    	|    	|    	|    	|    	|
  |+ Right        	|› `'`	| ⇧›  	|    	|    	|    	|    	|    	|    	|    	|
  |+ Optional     	|﹖ ?  	|   ⇧﹖	|    	|    	|    	|    	|    	|    	|    	|
  |Current Left   	|     	| S   	|  T 	| T  	|  O 	| O  	|  C 	| C  	| C  	|
  |...Right       	|     	| R   	|  W 	| W  	|  E 	| E  	|  Q 	| Q  	| Q  	|
  |...Any         	|     	| SS  	|  TT	| TT 	|  OO	| OO 	|  CC	| CC 	| CC 	|
  |+ Mandatory    	|!    	| !S  	|    	|    	|    	|    	|    	|    	|    	|
  |+ Optional     	|#    	| #S  	|    	|    	|    	|    	|    	|    	|    	|

Examples

  |New label      	| Side 	| Must/opt 	| Key    	|Old     	|
  |:--------------	| :-   	|  :-      	| :-     	| :-     	|
  |‹⇧             	| left 	| mandatory	| shift  	| !S     	|
  |⎈›             	| right	| mandatory	| control	| !W     	|
  |⎇›﹖            	| right	| optional 	| alt    	| #E     	|
  |⎇›⇧            	| right	| optional 	| alt    	| !Eshift	|

You can also input keys as strings to include whitespace which is helpful to achieve vertical alignment like so:
```edn
{:des "Vertically aligned modifiers in the FROM keys, note the mandatory ‘ at the beginning" :rules [
  ["‘‹⎈       k" :k]
  ["‘  ‹⇧     k" :k]
  ["‘   ⇧›    k" :k]
  ["‘   ⇧   ⎇k" :k]
  ["‘     ‹⌘  k" :k]
  ["‘       ⎇k" :k]
]}
```

__NB!__

  - Left/Right side indicators __must__ be at their respective sides (‹Left, Right›)
  - Optional﹖ indicator __must__ bet at the Right side and __after__ the side indicator (✓`⎈›﹖` ✗`⎈﹖›`)
  - Modifiers at the end of a key definition are treated as literal keys
  - Keys-as-strings __must__ have a `‘` in the beginning to differentiate them from, e.g., strings that contain script commands
  - User functions need to escape special symbols listed here with `_` (`[:e [:echo␠ "e"]]`→`[:e [:echo_␠ "e"]]`) to avoid conflict

### Other key symbols

  |Symbol(s)[^1] 	|Key `name`                                	|
  |---------     	|--------                                  	|
  |🌐 ƒ ⓕ Ⓕ 🄵 🅕 🅵 	|`!F`                                      	|
  |⇪             	|`P`  capslock                             	|
  |∀             	|`!A` any modifier regardless of side      	|
  |✱             	|`!!` hyper                                	|
  |∀﹖ ∀? ﹖﹖ ??   	|`##` optional any                         	|
  |⎋             	|`escape`                                  	|
  |⭾ ↹           	|`tab`                                     	|
  |␠ ␣           	|`spacebar`                                	|
  |␈ ⌫           	|`delete_or_backspace`                     	|
  |␡ ⌦           	|`delete_forward`                          	|
  |⏎ ↩ ⌤ ␤       	|`return_or_enter`                         	|
  |︔ ⸴ ．⁄        	|`semicolon` / `comma` / `period` / `slash`	|
  |“ ” ＂ « »     	|`quote`                                   	|
  |⧵ ＼           	|`backslash`                               	|
  |﹨             	|`non_us_backslash`                        	|
  |【 「 〔 ⎡       	|`open_bracket`                            	|
  |】 」 〕 ⎣       	|`close_bracket`                           	|
  |£             	|`non_us_pound`                            	|
  |ˋ ˜           	|`grave_accent_and_tilde`                  	|
  |‐ ₌           	|`hyphen` `equal_sign`                     	|
  |▲ ▼ ◀ ▶       	|`up`/`down`/`left`/`right` `_arrow`
  |⇞ ⇟           	|`page_` `up`/`down`                    	|
  |⎀             	|`insert`                               	|
  |⇤ ⤒ ↖         	|`home`                                 	|
  |⇥ ⤓ ↘         	|`end`                                  	|
  | ` `          	|no-break space removed                 	|
  |🔢₁ 🔢₂ 🔢₃ 🔢₄ 🔢₅	|`keypad_` `1`–`5`                      	|
  |🔢₆ 🔢₇ 🔢₈ 🔢₉ 🔢₀	|`keypad_` `6`–`0`                      	|
  |🔢₋ 🔢₌ 🔢₊      	|`keypad_` `hyphen`/`equal_sign`/`plus` 	|
  |🔢⁄ 🔢．🔢∗       	|`keypad_` `slash`/`period`/`asterisk`  	|
  |◀◀ ▶⏸ ▶▶      	|`vk_consumer_` `previous`/`play`/`next`	|
  |🔊 🔈+ or ➕₊⊕   	|`volume_up`                            	|
  |🔉 🔈− or ➖₋⊖   	|`volume_down`                          	|
  |🔇 🔈⓪ ⓿ ₀      	|`mute`                                 	|
  |🔆 🔅           	|`vk_consumer_brightness_` `up`/`down`  	|
  |⌨💡+ or ➕₊⊕    	|`vk_consumer_illumination_up`          	|
  |⌨💡− or ➖₋⊖    	|`vk_consumer_illumination_down`        	|
  |▦             	|`vk_launchpad`                         	|
  |🎛             	|`vk_dashboard`                         	|
  |▭▯            	|`vk_mission_control`                   	|
  |▤ ☰ 𝌆         	|`application`                          	|
  |🖰1 🖰2 ... 🖰32 	|`button` `1`–`32`                      	|

[^1]: space-separated list of keys; `or` means only last symbol in a pair changes

