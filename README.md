# Thaumcraft 4 Infusion Recipe Extension Library

This library provides an InfusionRecipe subclass with enhanced oredict handling capabilities.

The basic InfusionRecipe class offered by thaumcraft itself uses a broken oredict handling algorithm. 
It is broken in these ways:
1. Unconditionally and implicitly do oredict on all inputs. For instance, If your recipe require a specific type of wood (such as warpwood from Tainted Magic), thaumcraft will automatically allow any other kind of wood as well.
2. Only considers the first oredict name of the specified stack, and only check if any oredict name of player supplied ingredient is that first oredict name. This is problematic when items have more than one oredict name. 

This library, on the other hand, require mod author to give explicit instruction on whether oredict is needed.
It unifies various kinds of input under one interface RecipeIngredient.
Library itself provides a few implementation to handle common use cases and you should not need to implement your own subclass.

## Using this library in your project

### Within GTNH org

For mods with *Passive* or below support level, add `implementation("net.glease:tc4recipelib:<version>")` to `dependencies.gradle`

If the mod has *Active* support level, use `shadeCompile` configuration instead. 
Be sure to enable shadowing in `gradle.properties`

### For anyone else

This library is intended to be shadowed into your mod **without relocation**.
Alternatively, you could depend on an up-to-date version of my [TC4Tweaks](https://github.com/Glease/TC4Tweaks/).

Both are available at my maven:

```groovy
maven {
    name = "glee8e maven"
    url = "https://maven.glease.net/repos/releases/"
}
```

The standalone library is available at `"net.glease:tc4recipelib:<version>"`.
TC4Tweaks is available at `"net.glease:tc4tweaks:<version>"`.

Both artifact has `dev` variant for development environment. 
TC4Tweaks additionally has a `api` variant with everything in this library and any other TC4Tweaks api classes.

## API Status

Every class that is not package private is stable API.
Every public/protected member of stable API classes is stable API.

Stable API means a 100% forward compatibility guarantee without a major version number change.

## Relation with TC4Tweaks

This library was actually developed as part of TC4Tweaks (as evident in the package names) but later I decided a separate
project would be much easier for managing

Whenever a change in this library is made, a new TC4Tweaks with the updated library shadowed will be released. 
The new version of library will share the version number with that TC4Tweaks release
