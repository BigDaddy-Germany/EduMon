#!/bin/bash

app_path="${1?No app source path given}"
app_name="$(basename "$app_path" | sed -r "s/\.app$//g")"

echo "$app_name"

dmg_size="$(du -ms "$app_path" | grep -oP "^\d+")"
dmg_path=$(sed -r "s/\.app\/?$/.dmg/g" <<< "$app_path")

if [[ -e "$dmg_path" ]]
then
	echo "There is already a DMG at '$dmg_path' !"
	exit 1
fi

set -x

dd if=/dev/zero "of=$dmg_path" bs=1M "count=$dmg_size"
label=$(sed -r "s/\s/_/g" <<< "$app_name")
label=$(sed -r "s/^(.{,27}).*$/\1/g" <<< "$label")
hformat -l "$label" "$dmg_path"
mount_point="$(mktemp -d)"
mount -t hfs -o loop "$dmg_path" "$mount_point"
cp -R "$app_path" "$mount_point"
ls -l "$mount_point"
umount "$mount_point"
rm -R "$mount_point"

echo "DMG: $dmg_path -> $dmg_size MB"

du -h "$dmg_path"

