package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.FeatureLocal.*;
import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.descriptor.gradient.GradientFeatureProvider;
import org.openimaj.image.feature.local.descriptor.gradient.GradientFeatureProviderFactory;

/**
 * Created by root on 17-2-25.
 */
public class SpGradientFeatureExtractor implements SpScaleSpaceFeatureExtractor<OrientedFeatureVector, SpFImage> {


    SpAbstractDominantOrientationExtractor dominantOrientationExtractor;

    GradientFeatureProviderFactory factory;

    private SpGradientScaleSpaceImageExtractorProperties<SpFImage> currentGradientProperties = new SpGradientScaleSpaceImageExtractorProperties<SpFImage>();

    /**
     * The magnification factor determining the size of the sampling region
     * relative to the scale of the interest point.
     */
    protected float magnification = 12;

    /**
     * Construct with the given orientation extractor and gradient feature
     * provider. The default magnification factor of 12 is used.
     *
     * @param dominantOrientationExtractor
     *            the orientation extractor
     * @param factory
     *            the gradient feature provider
     */
    public SpGradientFeatureExtractor(SpAbstractDominantOrientationExtractor dominantOrientationExtractor,
                                    GradientFeatureProviderFactory factory)
    {
        this.dominantOrientationExtractor = dominantOrientationExtractor;
        this.factory = factory;
    }

    /**
     * Construct with the given orientation extractor, gradient feature provider
     * and magnification factor determining the size of the sampling region
     * relative to the scale of the interest point.
     *
     * @param dominantOrientationExtractor
     *            the orientation extractor
     * @param factory
     *            the gradient feature provider
     * @param magnification
     *            the magnification factor.
     */
    public SpGradientFeatureExtractor(SpAbstractDominantOrientationExtractor dominantOrientationExtractor,
                                    GradientFeatureProviderFactory factory, float magnification)
    {
        this(dominantOrientationExtractor, factory);
        this.magnification = magnification;
    }

    @Override
    public OrientedFeatureVector[] extractFeature(SpScaleSpaceImageExtractorProperties<SpFImage> properties) {
        final SpGradientScaleSpaceImageExtractorProperties<SpFImage> gprops = getCurrentGradientProps(properties);

        final float[] dominantOrientations = dominantOrientationExtractor.extractFeatureRaw(gprops);

        final OrientedFeatureVector[] ret = new OrientedFeatureVector[dominantOrientations.length];

        for (int i = 0; i < dominantOrientations.length; i++) {
            ret[i] = createFeature(dominantOrientations[i]);
        }

        return ret;
    }

    /**
     * Get the GradientScaleSpaceImageExtractorProperties for the given
     * properties. The returned properties are the same as the input properties,
     * but with the gradient images added.
     *
     * For efficiency, this method always returns the same cached
     * GradientScaleSpaceImageExtractorProperties, and internally updates this
     * as necessary. The gradient images are only recalculated when the input
     * image from the input properties is different to the cached one.
     *
     * @param properties
     *            input properties
     * @return cached GradientScaleSpaceImageExtractorProperties
     */
    public SpGradientScaleSpaceImageExtractorProperties<SpFImage> getCurrentGradientProps(
            SpScaleSpaceImageExtractorProperties<SpFImage> properties)
    {
        if (properties.image != currentGradientProperties.image) {
            currentGradientProperties.image = properties.image;

            // only if the size of the image has changed do we need to reset the
            // gradient and orientation images.
            if (currentGradientProperties.orientation == null ||
                    currentGradientProperties.orientation.height != currentGradientProperties.image.height ||
                    currentGradientProperties.orientation.width != currentGradientProperties.image.width)
            {
                currentGradientProperties.orientation = new SpFImage(currentGradientProperties.image.width,
                        currentGradientProperties.image.height);
                currentGradientProperties.magnitude = new SpFImage(currentGradientProperties.image.width,
                        currentGradientProperties.image.height);
            }

            SpFImageGradients.gradientMagnitudesAndOrientations(currentGradientProperties.image,
                    currentGradientProperties.magnitude, currentGradientProperties.orientation);
        }

        currentGradientProperties.x = properties.x;
        currentGradientProperties.y = properties.y;
        currentGradientProperties.scale = properties.scale;

        return currentGradientProperties;
    }

    /*
     * Iterate over the pixels in a sampling patch around the given feature
     * coordinates and pass the information to a feature provider that will
     * extract the relevant feature vector.
     */
    protected OrientedFeatureVector createFeature(final float orientation) {
        final float fx = currentGradientProperties.x;
        final float fy = currentGradientProperties.y;
        final float scale = currentGradientProperties.scale;

        // create a new feature provider and initialise it with the dominant
        // orientation
        final GradientFeatureProvider sfe = factory.newProvider();
        sfe.setPatchOrientation(orientation);

        // the integer coordinates of the patch
        final int ix = Math.round(fx);
        final int iy = Math.round(fy);

        final float sin = (float) Math.sin(orientation);
        final float cos = (float) Math.cos(orientation);

        // get the amount of extra sampling outside the unit square requested by
        // the feature
        final float oversampling = sfe.getOversamplingAmount();

        // this is the size of the unit bounding box of the patch in the image
        // in pixels
        final float boundingBoxSize = magnification * scale;

        // the amount of extra sampling per side in pixels
        final float extraSampling = oversampling * boundingBoxSize;

        // the actual sampling area is bigger than the boundingBoxSize by an
        // extraSampling on each side
        final float samplingBoxSize = extraSampling + boundingBoxSize + extraSampling;

        // In the image, the box (with sides parallel to the image frame) that
        // contains the
        // sampling box is:
        final float orientedSamplingBoxSize = Math.abs(sin * samplingBoxSize) + Math.abs(cos * samplingBoxSize);

        // now half the size and round to an int so we can iterate
        final int orientedSamplingBoxHalfSize = Math.round(orientedSamplingBoxSize / 2.0f);

        // get the images and their size
        final SpFImage mag = currentGradientProperties.magnitude;
        final SpFImage ori = currentGradientProperties.orientation;
        final int width = mag.width;
        final int height = mag.height;

        // now pass over all the pixels in the image that *might* contribute to
        // the sampling area
        for (int y = -orientedSamplingBoxHalfSize; y <= orientedSamplingBoxHalfSize; y++) {
            for (int x = -orientedSamplingBoxHalfSize; x <= orientedSamplingBoxHalfSize; x++) {
                final int px = x + ix;
                final int py = y + iy;

                // check if the pixel is in the image bounds; if not ignore it
                if (px >= 0 && px < width && py >= 0 && py < height) {
                    // calculate the actual position of the sample in the patch
                    // coordinate system
                    final float sx = 0.5f + ((-sin * y + cos * x) - (fx - ix)) / boundingBoxSize;
                    final float sy = 0.5f + ((cos * y + sin * x) - (fy - iy)) / boundingBoxSize;

                    // if the pixel is in the bounds of the sampling area then
                    // add it
                    if (sx > -oversampling && sx < 1 + oversampling && sy > -oversampling && sy < 1 + oversampling) {
                        sfe.addSample(sx, sy, mag.pixels[py][px], ori.pixels[py][px]);
                    }
                }
            }
        }

        return sfe.getFeatureVector();
    }
}
