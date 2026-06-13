package com.jujodevs.cursotestingandroid.core.mothers

import com.jujodevs.cursotestingandroid.core.builders.product

object ProductMother {
    val bread =
        product {
            withId("id-bread")
            withName("Pan")
            withDescription("Calentito")
            withCategory("breads")
            withPrice(2.5)
            withStock(8)
        }

    val milk =
        product {
            withId("id-milk")
            withName("Leche")
            withDescription("Entera")
            withCategory("dairy")
            withPrice(1.5)
            withStock(3)
        }

    val coffee =
        product {
            withId("id-coffee")
            withName("Café")
            withDescription("Molido")
            withCategory("drinks")
            withPrice(4.5)
            withStock(2)
        }

    val apple =
        product {
            withId("id-apple")
            withName("Manzana")
            withDescription("Roja y crujiente")
            withCategory("fruit")
            withPrice(0.8)
            withStock(20)
        }

    val yogurt =
        product {
            withId("id-yogurt")
            withName("Yogur")
            withDescription("Natural")
            withCategory("dairy")
            withPrice(0.5)
            withStock(12)
        }

    val chicken =
        product {
            withId("id-chicken")
            withName("Pollo")
            withDescription("Pechuga")
            withCategory("meat")
            withPrice(6.0)
            withStock(5)
        }
}
