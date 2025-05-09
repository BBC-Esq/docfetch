#!/bin/sh

# This script creates a disk image (.dmg) for macOS on Linux.
#
# Requirements:
# - mkfs.hfsplus must be on the PATH.
# - The script build.py must be run first.
# - Do not run this as root, see bug #412.
#
# Output:
# - build/DocFetcher-{version-number}.dmg

user=$(whoami)
if [ $user = "root" ]
then
	echo "Do not run this script as root."
	exit 0
fi

script=$(readlink -f "$0")
scriptdir=`dirname "$script"`
cd "$scriptdir"

version=`cat current-version.txt | grep -v "#" | head -1`

du_output=`du -sk build/DocFetcher.app 2>&1`
dir_size=`echo $du_output | cut -f1 -d" "`
dir_size=`expr $dir_size + 2048`
dmg_path=build/DocFetcher-$version.dmg

rm -f $dmg_path
dd if=/dev/zero of=$dmg_path bs=1024 count=$dir_size
mkfs.hfsplus -v "DocFetcher" $dmg_path

sudo mkdir /mnt/tmp-docfetcher
sudo mount -o loop $dmg_path /mnt/tmp-docfetcher

sudo cp -r build/DocFetcher.app /mnt/tmp-docfetcher
sudo chown -R $user:$user /mnt/tmp-docfetcher

sudo umount /mnt/tmp-docfetcher
sudo rm -rf /mnt/tmp-docfetcher
