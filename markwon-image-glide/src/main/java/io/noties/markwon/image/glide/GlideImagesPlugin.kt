package io.noties.markwon.image.glide

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spanned
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.AsyncDrawableLoader
import io.noties.markwon.image.AsyncDrawableScheduler.schedule
import io.noties.markwon.image.AsyncDrawableScheduler.unschedule
import io.noties.markwon.image.DrawableUtils.applyIntrinsicBoundsIfEmpty
import io.noties.markwon.image.ImageSpanFactory
import org.commonmark.node.Image

/**
 * @since 4.0.0
 */
class GlideImagesPlugin internal constructor(glideStore: GlideStore) : AbstractMarkwonPlugin() {
    interface GlideStore {
        fun load(drawable: AsyncDrawable): RequestBuilder<Drawable?>

        fun cancel(target: Target<*>)
    }

    private val glideAsyncDrawableLoader: GlideAsyncDrawableLoader =
        GlideAsyncDrawableLoader(glideStore)

    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        builder.setFactory(Image::class.java, ImageSpanFactory())
    }

    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
        builder.asyncDrawableLoader(glideAsyncDrawableLoader)
    }

    override fun beforeSetText(textView: TextView, markdown: Spanned) {
        unschedule(textView)
    }

    override fun afterSetText(textView: TextView) {
        schedule(textView)
    }

    private class GlideAsyncDrawableLoader(private val glideStore: GlideStore) :
        AsyncDrawableLoader() {
        private val cache: MutableMap<AsyncDrawable?, Target<*>?> =
            HashMap<AsyncDrawable?, Target<*>?>(2)

        override fun load(drawable: AsyncDrawable) {
            val target: CustomTarget<Drawable?> = AsyncDrawableTarget(drawable)
            cache.put(drawable, target)
            glideStore.load(drawable).into<Target<Drawable?>?>(target)
        }

        override fun cancel(drawable: AsyncDrawable) {
            val target = cache.remove(drawable)
            if (target != null) {
                glideStore.cancel(target)
            }
        }

        override fun placeholder(drawable: AsyncDrawable): Drawable? {
            return null
        }

        private inner class AsyncDrawableTarget(private val drawable: AsyncDrawable) :
            CustomTarget<Drawable?>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable?>?
            ) {
                if (cache.remove(drawable) != null) {
                    if (drawable.isAttached) {
                        applyIntrinsicBoundsIfEmpty(resource)
                        drawable.result = resource
                    }
                }
            }

            override fun onLoadStarted(placeholder: Drawable?) {
                if (placeholder != null && drawable.isAttached) {
                    applyIntrinsicBoundsIfEmpty(placeholder)
                    drawable.result = placeholder
                }
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                if (cache.remove(drawable) != null) {
                    if (errorDrawable != null && drawable.isAttached) {
                        applyIntrinsicBoundsIfEmpty(errorDrawable)
                        drawable.result = errorDrawable
                    }
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                // we won't be checking if target is still present as cancellation
                // must remove target anyway
                if (drawable.isAttached) {
                    drawable.clearResult()
                }
            }
        }
    }

    companion object {
        fun create(context: Context): GlideImagesPlugin {
            // @since 4.5.0 cache RequestManager
            //  sometimes `cancel` would be called after activity is destroyed,
            //  so `Glide.with(context)` will throw an exception
            return create(Glide.with(context))
        }

        fun create(requestManager: RequestManager): GlideImagesPlugin {
            return create(object : GlideStore {
                override fun load(drawable: AsyncDrawable): RequestBuilder<Drawable?> {
                    return requestManager.load(drawable.destination)
                }

                override fun cancel(target: Target<*>) {
                    requestManager.clear(target)
                }
            })
        }

        fun create(glideStore: GlideStore): GlideImagesPlugin {
            return GlideImagesPlugin(glideStore)
        }
    }
}
