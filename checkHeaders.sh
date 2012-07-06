#!/bin/sh

EXPECT=" * Copyright (c) "
LEXPECT=${#EXPECT}

for file in $(find . -type f -name "*.java"); do
	line2=$(head -n 2 "${file}" | tail -n 1)
	l2start=$(expr substr "$line2" 1 ${LEXPECT})
	if [ "${EXPECT}" != "${l2start}" ]; then
		echo "no copyright notice in: $file"
	fi
done
