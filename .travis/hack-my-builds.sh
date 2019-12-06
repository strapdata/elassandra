#!/usr/bin/env bash
set -Eeuo pipefail

echo >&2 'WARNING: this script modifies /etc/docker/daemon.json; use at your own peril'

# uses https://github.com/tianon/rawdns to install pgp-happy-eyeballs in a way that can be used for "curl|bash" in Travis builds for transparently happy eyeballs

set -x

bip="$(ip address show dev docker0 | awk '$1 == "inet" { print $2; exit }')"
[ -n "$bip" ]
ip="${bip%/*}"
[ -n "$ip" ]

uid="$(id -u)"
_sudo() {
	if [ "$uid" = '0' ]; then
		"$@"
	else
		sudo "$@"
	fi
}

_sudo sh -xec '
	mkdir -p /etc/docker
	[ -s /etc/docker/daemon.json ] || echo "{}" > /etc/docker/daemon.json
'

_sudo jq --arg bip "$bip" --arg ip "$ip" '
		.bip = $bip
		| .dns = [
			$ip,
			"1.1.1.1",
			"1.0.0.1"
		]
	' /etc/docker/daemon.json \
		| sed 's/  /\t/g' \
		| _sudo tee /etc/docker/daemon.json.rawdns \
		> /dev/null
_sudo mv /etc/docker/daemon.json.rawdns /etc/docker/daemon.json
if [ -d /run/systemd/system ] && command -v systemctl; then
	_sudo systemctl --quiet stop docker || :
	_sudo systemctl start docker
	_sudo systemctl --full --no-pager status docker
else
	_sudo service docker stop &> /dev/null || :
	_sudo service docker start
fi
docker version > /dev/null

docker rm -vf rawdns &> /dev/null || :
docker run -d \
	--restart always \
	--name rawdns \
	-v /var/run/docker.sock:/var/run/docker.sock \
	-p "$ip":53:53/tcp \
	-p "$ip":53:53/udp \
	--dns 1.1.1.1 \
	--dns 1.0.0.1 \
	tianon/rawdns sh -xec '
		cat > /rawdns.json <<-"EOF"
			{
				"keyserver.ubuntu.com.": { "type": "static", "cnames": [ "pgp-happy-eyeballs.docker" ], "nameservers": [ "127.0.0.1" ] },
				"pgp.mit.edu.": { "type": "static", "cnames": [ "pgp-happy-eyeballs.docker" ], "nameservers": [ "127.0.0.1" ] },
				"pool.sks-keyservers.net.": { "type": "static", "cnames": [ "pgp-happy-eyeballs.docker" ], "nameservers": [ "127.0.0.1" ] },
				"hkps.pool.sks-keyservers.net.": { "type": "forwarding", "nameservers": [ "1.1.1.1", "1.0.0.1" ] },

				"docker.": { "type": "containers", "socket": "unix:///var/run/docker.sock" },
				".": { "type": "forwarding", "nameservers": [ "1.1.1.1", "1.0.0.1" ] }
			}
		EOF
		exec rawdns /rawdns.json
	'
docker rm -vf pgp-happy-eyeballs &> /dev/null || :
docker run -d --restart always --name pgp-happy-eyeballs --dns 1.1.1.1 --dns 1.0.0.1 tianon/pgp-happy-eyeballs

# trust, but verify
docker run --rm tianon/network-toolbox:alpine gpg --keyserver fake.pool.sks-keyservers.net --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4
docker logs pgp-happy-eyeballs
