#!/bin/bash

script_dir=$(cd `dirname $0`; pwd);
files_dir=$(cd $script_dir/../files; pwd);

source ../aegis-docker/bin/aegis-config;
export container_name=aegis-jm-dev
export project_name=aegis-jm
export image_name=aegis-jm
if [[ "$@" = "staging" ]]; then
    export create_param="-p 8092:8080 \
-v ${files_dir}:/app/files \
-v ${script_dir}/logs:/app/aegis-jm/logs \
-v ${script_dir}/docker-config-staging:/app/aegis-jm/config"
elif [[ "$@" = "testing" ]]; then
    export create_param="-p 8092:8080 \
-v ${files_dir}:/app/files \
-v ${script_dir}/logs:/app/aegis-jm/logs \
-v ${script_dir}/docker-config-testing:/app/aegis-jm/config"
else
    export create_param="-p 8092:8080 \
-v ${files_dir}:/app/files \
-v ${script_dir}/logs:/app/aegis-jm/logs
-v ${script_dir}/docker-config-dev:/app/aegis-jm/config"
fi
export build_type=mvn
export ip=${aegis_jm_ip};

mbt $@;

