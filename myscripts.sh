#!/bin/bash

source_dir="/dataset"
target_dir="lyy@10.254.22.70:/home/lyy/"

for file in "$source_dir"/*; do
    if [ -f "$file" ]; then
        scp "$file" "$target_dir" || echo "Failed to upload file: $file"
    fi
done
