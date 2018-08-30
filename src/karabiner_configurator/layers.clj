(ns karabiner-configurator.layers
  (:require
   [karabiner-configurator.data :refer :all]
   [karabiner-configurator.misc :refer :all]))


(def example-layers {:chunkwm-mode {:from :tab}})

(defn parse-layers [layers]
  (if (nn? layers)
    (for [[layer-name layer-info] layers]
      (let [{:keys [from-value]} layer-info]))))
        ;; (if (k? from-value)
        ;;   {:from {:key_code from-value}}
        ;;   (if (froms)))))))

(parse-layers example-layers)

(defn parse-simlayers [simlayers])

