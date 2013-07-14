#!/bin/sh

check_headers () {
	EXPECT=" * Copyright (c) "
	LEXPECT=${#EXPECT}

	for file in $(find . -type f -name "*.java"); do
		line2=$(head -n 2 "${file}" | tail -n 1)
		l2start=$(expr substr "$line2" 1 ${LEXPECT})
		if [ "${EXPECT}" != "${l2start}" ]; then
			echo "no copyright notice in: $file"
		fi
	done
}

check_updates () {
	mvn versions:display-dependency-updates versions:display-plugin-updates versions:display-property-updates
}

check_coverage() {
	mvn cobertura:cobertura
}

case $1 in
	headers)
		check_headers
		;;
	updates)
		check_updates
		;;
	coverage)
		check_coverage
		;;
	all)
		check_headers
		check_updates
		check_coverage
		;;
	*)
		echo "$0 <which check>"
		echo "checks:"
		echo "  all"
		echo "  headers"
		echo "  updates"
		echo "  coverage"
		;;
esac
