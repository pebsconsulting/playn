/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.java;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import playn.core.AbstractAssetManager;
import playn.core.Image;
import playn.core.PlayN;
import playn.core.ResourceCallback;
import playn.core.Sound;

/**
 * Loads Java assets via the classpath.
 */
public class JavaAssetManager extends AbstractAssetManager {

  private String pathPrefix = "";

  /**
   * Configures the prefix prepended to asset paths before fetching them from the classpath. For
   * example, if your assets are in {@code src/main/java/com/mygame/assets} (or in {@code
   * src/main/resources/com/mygame/assets}), you can pass {@code com/mygame/assets} to this method
   * and then load your assets without prefixing their path with that value every time. The value
   * supplied to this method should not contain leading or trailing slashes. Note that this prefix
   * should always use '/' as a path separator as it is used to construct URLs, not filesystem
   * paths.
   */
  public void setPathPrefix(String prefix) {
    if (prefix.startsWith("/") || prefix.endsWith("/")) {
      throw new IllegalArgumentException("Prefix must not start or end with '/'.");
    }
    pathPrefix = (prefix.length() == 0) ? prefix : (prefix + "/");
  }

  /**
   * Returns the currently configured path prefix. Note that this value will always have a trailing
   * slash.
   */
  public String getPathPrefix() {
      return pathPrefix;
  }

  @Override
  protected Image doGetImage(String path) {
    try {
      return new JavaImage(ImageIO.read(requireResource(pathPrefix + path)));
    } catch (Exception e) {
      PlayN.log().warn("Could not load image at " + pathPrefix + path, e);
      return new JavaImage(e);
    }
  }

  @Override
  protected Sound doGetSound(String path) {
    path += ".mp3";
    InputStream in = getClass().getClassLoader().getResourceAsStream(pathPrefix + path);
    if (in == null) {
      PlayN.log().warn("Could not find sound " + pathPrefix + path);
      return ((JavaAudio) PlayN.audio()).createNoopSound();
    } else {
      return ((JavaAudio) PlayN.audio()).createSound(path, in);
    }
  }

  @Override
  protected void doGetText(String path, ResourceCallback<String> callback) {
    try {
      callback.done(Resources.toString(requireResource(pathPrefix + path), Charsets.UTF_8));
    } catch (Exception e) {
      callback.error(e);
    }
  }

  protected URL requireResource(String path) throws FileNotFoundException {
      URL url = getClass().getClassLoader().getResource(path);
      if (url == null) {
        throw new FileNotFoundException(path);
      }
      return url;
  }
}
